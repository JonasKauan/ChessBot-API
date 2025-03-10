package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.zika.chessbot.bot.utils.BishopUtils;
import com.zika.chessbot.bot.utils.KingUtils;
import com.zika.chessbot.bot.utils.RookUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardEvaluator {

    private final static int[][] CENTER_BONUS = {
            {3, 4, 4, 5, 5, 4, 4, 3},
            {4, 6, 6, 8, 8, 6, 6, 4},
            {4, 6, 7, 10, 10, 7, 6, 4},
            {5, 8, 10, 12, 12, 10, 8, 5},
            {5, 8, 10, 12, 12, 10, 8, 5},
            {4, 6, 7, 10, 10, 7, 6, 4},
            {4, 6, 6, 8, 8, 6, 6, 4},
            {3, 4, 4, 5, 5, 4, 4, 3}
    };

    private final static int[][] KING_CENTER_BONUS = {
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 5, 10, 15, 15, 10, 5, 0},
            {0, 10, 20, 30, 30, 20, 10, 0},
            {0, 15, 30, 50, 50, 30, 15, 0},
            {0, 15, 30, 50, 50, 30, 15, 0},
            {0, 10, 20, 30, 30, 20, 10, 0},
            {0, 5, 10, 15, 15, 10, 5, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}
    };

    private final BishopUtils bishopUtils;
    private final RookUtils rookUtils;
    private final KingUtils kingUtils;

    public int evaluate(Board board) {
        int evaluation = calculateScore(board, Side.WHITE) - calculateScore(board, Side.BLACK);
        return evaluation * (board.getSideToMove() == Side.WHITE ? 1 : -1);
    }

    public int calculateContemptFactor(Board board) {
        double contempt = -.5;
        int evaluation = evaluate(board);
        return (int) (contempt * calculateEndGameWeigth(board) * (evaluation > 0 ? 1 : -1));
    }

    private int calculateScore(Board board, Side side) {
        int score = 0;
        Square[] squares = Square.values();

        for(Square square : squares) {
            Piece piece = board.getPiece(square);

            if(piece == Piece.NONE || piece.getPieceSide() != side) {
                continue;
            }

            Weights pieceWeights = Weights.valueOf(piece.toString());

            score += pieceWeights.getPieceWeight();
            score += pieceWeights.getPositionWeight()[square.getRank().ordinal()][square.getFile().ordinal()];
            score += evaluateStrategicPosition(square, board);

            if(piece.getPieceType() == PieceType.PAWN) {
                if(isPassedPawn(square, board)) score += 50;
                if(BoardUtils.isEndGame(board)) score += 10 / getDistanceToEndBoard(square, piece.getPieceSide());
            }
        }

        return score;
    }

    private boolean isPassedPawn(Square square, Board board) {
        Piece piece = board.getPiece(square);

        String opSideFenChar = piece.getPieceSide() == Side.WHITE
                ? piece.getFenSymbol().toLowerCase()
                : piece.getFenSymbol().toUpperCase();

        long enemyPawnBitBoard = board.getBitboard(Piece.fromFenSymbol(opSideFenChar));
        long passedPawnMask = getPassedPawnMask(square, piece.getPieceSide());

        return (passedPawnMask & enemyPawnBitBoard) == 0;
    }

    private long getPassedPawnMask(Square square, Side side) {
        long columnMask = BoardUtils.A_FILE_BIT_BOARD << square.getFile().ordinal()
                | BoardUtils.A_FILE_BIT_BOARD << Math.max(0, square.getFile().ordinal() - 1)
                | BoardUtils.A_FILE_BIT_BOARD << Math.min(7, square.getFile().ordinal() + 1);

        if(side == Side.WHITE) {
            return (-1L << 8 * (square.getRank().ordinal() + 1)) & columnMask;
        }

        int rankIndex = 8 - square.getRank().ordinal();

        return (-1L >>> 8 * rankIndex) & columnMask;
    }

    private int getDistanceToEndBoard(Square square, Side side) {
        if(side == Side.WHITE) {
            return 7 - square.getRank().ordinal();
        }

        return square.getRank().ordinal();
    }

    public double calculateEndGameWeigth(Board board) {
        long bitBoard = board.getBitboard();
        int numberPieces = 0;

        for(int i = 0; i < 64; i++){
            long bit = 1L << i;
            if((bitBoard & bit) == 0) continue;
            numberPieces++;
        }

        return numberPieces / 32.;
    }

    public int evaluateCapture(Move move, Board board) {
        int score = MVVLVA(move, board);

        if(BoardUtils.isSquareAttacked(move.getTo(), board)) {
            score -= Weights.valueOf(board.getPiece(move.getFrom()).toString()).getPieceWeight();
        }

        return score + evaluateMobility(move, board) + evaluateStrategicPosition(move, board);
    }

    public int evaluateQuietMove(Move move, Board board) {
        Piece piece = board.getPiece(move.getFrom());
        int[][] movedPiecePositionWeights = Weights.valueOf(piece.toString()).getPositionWeight();

        Square from = move.getFrom();
        Square to = move.getTo();

        int pstEvaluation = movedPiecePositionWeights[to.getRank().ordinal()][to.getFile().ordinal()]
                - movedPiecePositionWeights[from.getRank().ordinal()][from.getFile().ordinal()];

        return pstEvaluation + evaluateMobility(move, board) + evaluateStrategicPosition(move, board);
    }

    private int evaluateStrategicPosition(Move move, Board board) {
        return switch(board.getPiece(move.getFrom()).getPieceType()) {
            case BISHOP -> evaluateDiagonalControl(move, board) + evaluateBlockPenalty(move.getTo(), board);
            case ROOK -> evaluateFileControl(move.getTo(), board);
            case QUEEN -> evaluateDiagonalControl(move, board) + evaluateFileControl(move.getTo(), board);
            case KING -> evaluateKingSafety(move.getTo(), board);
            default -> evaluateCenterControl(move.getTo(), move.getFrom());
        };
    }

    private int evaluateStrategicPosition(Square square, Board board) {
        return switch(board.getPiece(square).getPieceType()) {
            case BISHOP -> evaluateDiagonalControl(square, board) + evaluateBlockPenalty(square, board);
            case ROOK -> evaluateFileControl(square, board);
            case QUEEN -> evaluateDiagonalControl(square, board) + evaluateFileControl(square, board);
            case KING -> evaluateKingSafety(square, board);
            default -> evaluateCenterControl(square);
        };
    }

    private int evaluateKingSafety(Square square, Board board) {
        if(BoardUtils.isEndGame(board)) {
            return KING_CENTER_BONUS[square.getRank().ordinal()][square.getFile().ordinal()];
        }

        return kingUtils.countAttackersNearKingZone(square, board) * -20;
    }

    private int evaluateFileControl(Square square, Board board) {
        return (int) (rookUtils.getFileOpenness(square, board) * 30);
    }

    private int evaluateBlockPenalty(Square square, Board board) {
        return -bishopUtils.countBlockedDiagonals(board, square) * 10;
    }

    private int evaluateDiagonalControl(Move move, Board board) {
        return bishopUtils.countDiagonalMoves(board, move.getTo())
                - bishopUtils.countDiagonalMoves(board, move.getFrom());
    }

    private int evaluateDiagonalControl(Square square, Board board) {
        return bishopUtils.countDiagonalMoves(board, square);
    }

    private int evaluateCenterControl(Square to, Square from) {
        return CENTER_BONUS[to.getRank().ordinal()][to.getFile().ordinal()]
                - CENTER_BONUS[from.getRank().ordinal()][from.getFile().ordinal()];
    }

    private int evaluateCenterControl(Square square) {
        return CENTER_BONUS[square.getRank().ordinal()][square.getFile().ordinal()];
    }

    private int evaluateMobility(Move move, Board board) {
        Side side = move.getPromotion().getPieceSide();

        int mobilityBefore = board.legalMoves()
                .stream()
                .filter(m -> m.getPromotion().getPieceSide() == side)
                .toList()
                .size();

        board.doMove(move);

        int mobilityAfter = board.legalMoves()
                .stream()
                .filter(m -> m.getPromotion().getPieceSide() == side)
                .toList()
                .size();

        board.undoMove();
        return mobilityAfter - mobilityBefore;
    }

    private int MVVLVA(Move move, Board board) {
        Weights capturedPieceWeight = Weights.valueOf(board.getPiece(move.getTo()).toString());
        Weights capturerPieceWeight = Weights.valueOf(board.getPiece(move.getFrom()).toString());

        return capturedPieceWeight.getPieceWeight() * 10 - capturerPieceWeight.getPieceWeight();
    }

    public int SEE(Move capture, Board board) {
        int value = 0;
        Piece piece = board.getPiece(capture.getTo());

        if(piece == Piece.NONE) {
            return value;
        }

        board.doMove(capture);

        Move counterCapture = board.legalMoves()
                .stream()
                .filter(m -> m.getTo() == capture.getTo())
                .findFirst()
                .orElse(null);

        int capturedPieceValue = Weights.valueOf(piece.toString()).getPieceWeight();

        if(counterCapture == null) {
            board.undoMove();
            return capturedPieceValue;
        }

        value = Math.max(0, capturedPieceValue - SEE(counterCapture, board));
        board.undoMove();

        return value;
    }
}
