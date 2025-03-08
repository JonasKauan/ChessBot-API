package com.zika.chessbot.bot;

import java.util.List;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Rank;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// TODO: Ele parece não estar achando o mate do jeito mais rápido, fazer algo a respeito

@Slf4j
@Service
@RequiredArgsConstructor
public class ChessBot {
    private final BoardEvaluator boardEvaluator;
    private final SanParser sanParser;
    private final MoveOrderer moveOrderer;
    private final OpeningBook openingBook;
    private final ZorbristHash zorbrist;
    private final TranspositionTable tTable;
    private static final int INFINITY = 200_000;
    private static final int LOOKUP_FAILED = Integer.MIN_VALUE;
    private static final int MAX_NUMBER_EXTENSION = 16;
    private static final int MATE_SCORE = Integer.MAX_VALUE;
    private static final long TIME_TO_THINK = 500;
    private static final int VAL_WINDOW = 50;

    public String decideMove(String fenString) {
        Board board = new Board();
        board.loadFromFen(fenString);

        Move openingMove = openingBook.getOpeningMove(board);

        return openingMove == null ? search(board) : sanParser.parseToSan(board, openingMove);
    }

    private String search(Board board) {
        int alpha = -INFINITY;
        int beta = INFINITY;

        Move bestMove = null;

        long searchStartTime = System.currentTimeMillis();
        KillerMoves killerMoves = new KillerMoves();

        for(int depth = 1; true; depth++) {
            int bestScore = -INFINITY;
            int aspirationWindow = VAL_WINDOW;

            boolean aspirationWindowAdjusted = false;
            int failCount = 0;

            while(!aspirationWindowAdjusted) {
                if(failCount > 2) {
                    alpha = -INFINITY;
                    beta = INFINITY;
                }

                for(Move move : moveOrderer.getOrderedMoves(board, bestMove, killerMoves, null)) {
                    board.doMove(move);
                    double halfPlyDepth = depth * 2 - 2;
                    int score = -negamax(halfPlyDepth, alpha, beta, 0, MATE_SCORE, board, killerMoves);
                    board.undoMove();

                    if(score > bestScore) {
                        bestMove = move;
                        bestScore = score;
                    }

                    if(System.currentTimeMillis() - searchStartTime >= TIME_TO_THINK) {
                        log.info("Busca terminada após profundidade de {}", depth);
                        log.info("Melhor movimento encontrado {}", bestMove);
                        return sanParser.parseToSan(board, bestMove);
                    }
                }

                if(bestScore <= alpha || bestScore >= beta) {
                    alpha = bestScore - aspirationWindow;
                    beta = bestScore + aspirationWindow;
                    aspirationWindow += aspirationWindow / 2;
                    failCount++;
                    continue;
                }

                alpha = bestScore - VAL_WINDOW;
                beta = bestScore + VAL_WINDOW;
                aspirationWindowAdjusted = true;
            }
        }
    }

    private int negamax(
            double halfPlyDepth,
            int alpha,
            int beta,
            double totalExtensions,
            int mate,
            Board board,
            KillerMoves killerMoves
    ) {
        if(halfPlyDepth <= 0) {
            return quiescenceSearch(alpha, beta, board);
        }

        int fullPlyDepth = (int) (halfPlyDepth / 2);

        long hashKey = zorbrist.generateHash(board);
        int transpositionLookup = tTable.lookupEvaluation(fullPlyDepth, alpha, beta, hashKey);

        if(transpositionLookup != LOOKUP_FAILED) {
            return transpositionLookup;
        }

        List<Move> moves = moveOrderer.getOrderedMoves(board, null, killerMoves, fullPlyDepth);

        if(moves.isEmpty()) {
            if(board.isKingAttacked()) {
                return -mate;
            }

            return boardEvaluator.calculateContemptFactor(board);
        }

        Flag flag = Flag.UPPERBOUND;
        boolean pvNode = false;
        Move bestMove = null;

        for(Move move : moves) {
            Piece capturedPiece = board.getPiece(move.getTo());
            boolean canRecapture = BoardUtils.isSquareAttacked(move.getTo(), board);
            board.doMove(move);

            double extension = calculateSearchExtension(totalExtensions, board, pvNode, capturedPiece, move, canRecapture);
            totalExtensions = Math.min(totalExtensions + extension, MAX_NUMBER_EXTENSION);
            double actualDepth = halfPlyDepth - 1 + extension;

            int score;

            if(pvNode) {
                score = -negamax(actualDepth, -alpha - 1, -alpha, totalExtensions, mate - 1, board, killerMoves);

                if(score > alpha && score < beta)
                    score = -negamax(actualDepth, -beta, -alpha, totalExtensions, mate - 1, board, killerMoves);
            } else {
                score = -negamax(actualDepth, -beta, -alpha, totalExtensions, mate - 1, board, killerMoves);
            }
            
            board.undoMove();

            if(score >= beta) {
                tTable.setEntry(hashKey, Flag.LOWERBOUND, move, beta, fullPlyDepth);
                killerMoves.storeKillerMove(move, fullPlyDepth, board);
                return beta;
            }

            if(score > alpha) {
                flag = Flag.EXACT;
                alpha = score;
                bestMove = move;
                pvNode = true;
            }
        }

        tTable.setEntry(hashKey, flag, bestMove, alpha, fullPlyDepth);
        return alpha;
    }

    private double calculateSearchExtension(
            double totalExtensions,
            Board board,
            boolean pvNode,
            Piece capturedPiece,
            Move move,
            boolean canRecapture
    ) {
        double extension = 0;

        if(totalExtensions < MAX_NUMBER_EXTENSION) {
            if(board.isKingAttacked()) {
                extension = board.legalMoves().size() > 1 ? .5 : 1;
            }

            // TODO: Fazer essas extenções de um jeito mais inteligente

//            if(pvNode) {
//                extension = Math.max(extension, .5);
//            }
//
//            if(capturedPiece != Piece.NONE && canRecapture) {
//                extension = 1;
//            }
//
//            if(
//                board.getPiece(move.getTo()).getPieceType() == PieceType.PAWN
//                && (move.getTo().getRank() == Rank.RANK_1 || move.getTo().getRank() == Rank.RANK_8)
//            ) {
//                extension = 1;
//            }
        }

        return extension;
    }

    private int quiescenceSearch(int alpha, int beta, Board board) {
        int standPat = boardEvaluator.evaluate(board);

        if(standPat >= beta) {
            return beta;
        }

        alpha = Math.max(alpha, standPat);

        for(Move capture : moveOrderer.getCaptures(board, null)) {
            if(boardEvaluator.SEE(capture, board) == 0) {
                continue;
            }

            board.doMove(capture);
            int score = -quiescenceSearch(-beta, -alpha, board);
            board.undoMove();

            double delta = score - standPat;

            if(delta >= 0) {
                return score;
            }

            if(score >= beta) {
                return beta;
            }

            alpha = Math.max(alpha, score);
        }

        return alpha;
    }
}
