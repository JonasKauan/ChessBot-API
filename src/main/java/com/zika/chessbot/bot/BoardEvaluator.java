package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import org.springframework.stereotype.Service;

@Service
public class BoardEvaluator {

    int[][] CENTER_BONUS = {
            {3, 4, 4, 5, 5, 4, 4, 3},
            {4, 6, 6, 8, 8, 6, 6, 4},
            {4, 6, 7, 10, 10, 7, 6, 4},
            {5, 8, 10, 12, 12, 10, 8, 5},
            {5, 8, 10, 12, 12, 10, 8, 5},
            {4, 6, 7, 10, 10, 7, 6, 4},
            {4, 6, 6, 8, 8, 6, 6, 4},
            {3, 4, 4, 5, 5, 4, 4, 3}
    };


    public int evaluate(Board board) {
        int evaluation = calculateScore(board, Side.WHITE) - calculateScore(board, Side.BLACK);
        return evaluation * (board.getSideToMove() == Side.WHITE ? 1 : -1);
    }

    public double calculateContemptFactor(Board board){
        double contempt = -.5;
        int evaluation = evaluate(board);
        return contempt * calculateEndGameWeigth(board) * (evaluation > 0 ? 1 : -1);
    }

    public double calculateEndGameWeigth(Board board){
        long bitBoard = board.getBitboard();
        int numberPieces = 0;

        for(int i = 0; i < 64; i++){
            long bit = 1L << i;
            if((bitBoard & bit) == 0) continue;
            numberPieces++;
        }

        return numberPieces / 32.;
    }

    private int calculateScore(Board board, Side side) {
        PieceType[] pieces = PieceType.values();
        
        int score = 0;
        for(PieceType type : pieces){
            if(type == PieceType.NONE) continue;    
            score += quantifyPieces(board, type, side);
        }

        return score;
    }

    private int quantifyPieces(Board board, PieceType type, Side side) {
        long pieceBitBoard = board.getBitboard(Piece.valueOf(side.toString()+"_"+type.toString())) & board.getBitboard();

        int piecesCount = 0;
        Weights w = Weights.valueOf(side.toString()+"_"+type.toString());

        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                long bit = 1L << (i * 8 + j);
                if((pieceBitBoard & bit) == 0) continue;
                piecesCount += w.getPieceWeight() + w.getPositionWeight()[i][j];
            }
        }

        return piecesCount;
    }

    public int evaluateQuietMove(Move move, Board board) {
        Piece piece = board.getPiece(move.getFrom());
        PieceType movedPieceType = piece.getPieceType();

        int[][] movedPiecePositionWeights = Weights.valueOf(piece.toString()).getPositionWeight();

        Square from = move.getFrom();
        Square to = move.getTo();

        int pstEvaluation = movedPiecePositionWeights[to.getRank().ordinal()][to.getFile().ordinal()]
                - movedPiecePositionWeights[from.getRank().ordinal()][from.getFile().ordinal()];

        int mobilityEval = evaluateMobility(move, board);
        int centerControl = evaluateCenterControl(from, to);

        return pstEvaluation + mobilityEval + centerControl;
    }

    private int evaluateCenterControl(Square from, Square to) {
        return CENTER_BONUS[to.getRank().ordinal()][to.getFile().ordinal()]
                - CENTER_BONUS[from.getRank().ordinal()][from.getFile().ordinal()];
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

    public int MVV_LVA(Move move, Board board) {
        Weights capturedPieceWeight = Weights.valueOf(board.getPiece(move.getTo()).toString());
        Weights capturerPieceWeight = Weights.valueOf(board.getPiece(move.getFrom()).toString());

        return capturedPieceWeight.getPieceWeight() * 10 - capturerPieceWeight.getPieceWeight();
    }
}
