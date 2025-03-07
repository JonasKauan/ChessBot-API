package com.zika.chessbot.bot;

import java.util.List;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private static final int INFINITY = 9_999_999;
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
                    int score = -negamax(depth * 2 - 2, alpha, beta, 0, MATE_SCORE, board, killerMoves);
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
            int totalExtensions,
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
            board.doMove(move);
            int extension = calculateSearchExtension(totalExtensions, board);
            int score;

            if(pvNode) {
                score = -negamax(halfPlyDepth - 2 + extension, -alpha - 1, -alpha, totalExtensions + extension, mate - 1, board, killerMoves);

                if(score > alpha && score < beta)
                    score = -negamax(halfPlyDepth - 2 + extension, -beta, -alpha, totalExtensions + extension, mate - 1, board, killerMoves);
            } else {
                score = -negamax(halfPlyDepth - 2 + extension, -beta, -alpha, totalExtensions + extension, mate - 1, board, killerMoves);
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

    private int calculateSearchExtension(int totalExtensions, Board board) {
        int extension = 0;

        if(totalExtensions < MAX_NUMBER_EXTENSION) {
            if(board.isKingAttacked()) extension = 1;
        }

        return extension * 2;
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
