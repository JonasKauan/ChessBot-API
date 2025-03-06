package com.zika.chessbot.bot;

import java.util.List;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// TODO Implementar Aspiration window

@Slf4j
@Service
@RequiredArgsConstructor
public class ChessBot {
    private final BoardEvaluator boardEvaluator;
    private final SanParser sanParser;
    private final MoveOrderer moveOrderer;
    private final OpeningBook openingBook;
    private final ZorbristHash zorbrist;
    private static final int INFINITY = 9999999;
    private static final int LOOKUP_FAILED = Integer.MIN_VALUE;
    private static final int MAX_NUMBER_EXTENSION = 16;
    private static final int MATE_SCORE = Integer.MAX_VALUE;
    private static final long TIME_TO_THINK = 100;

    public String decideMove(String fenString) {
        Board board = new Board();
        board.loadFromFen(fenString);

        Move openingMove = openingBook.getOpeningMove(board);

        return openingMove == null ? search(board) : sanParser.parseToSan(board, openingMove);
    }

    private String search(Board board) {
        TranspositionTable tTable = new TranspositionTable();

        int alpha = -INFINITY;
        int beta = INFINITY;

        Move bestMove = null;

        long searchStartTime = System.currentTimeMillis();
        for(int depth = 1; true; depth++) {
            KillerMoves killerMoves = new KillerMoves();
            double bestScore = -INFINITY;

            for(Move move : moveOrderer.getOrderedMoves(board, bestMove, killerMoves, null)) {
                board.doMove(move);
                double score = -negamax(depth - 1, alpha, beta, 0, MATE_SCORE, board, tTable, searchStartTime, killerMoves);
                board.undoMove();

                if (score == MATE_SCORE || score == -MATE_SCORE) {
                    return sanParser.parseToSan(board, move);
                }

                if(score == -INFINITY) {
                    log.info("Busca terminada após profundidade de {}", depth);
                    log.info("Melhor movimento encontrado {}", bestMove);
                    return sanParser.parseToSan(board, bestMove);
                }

                if(score >= bestScore) {
                    bestMove = move;
                    bestScore = score;
                }
            }
        }
    }

    public int negamax(
            int depth,
            int alpha,
            int beta,
            int totalExtensions,
            int mate,
            Board board,
            TranspositionTable tTable,
            long searchStartTime,
            KillerMoves killerMoves
    ) {
        if(maxSearchTimeExceeded(searchStartTime)) {
            return INFINITY;
        }

        if(depth == 0) {
            return quiescenceSearch(alpha, beta, board);
        }

        long hashKey = zorbrist.generateHash(board);
        int transpositionLookup = tTable.lookupEvaluation(board, depth, alpha, beta, hashKey);

        if(transpositionLookup != LOOKUP_FAILED) {
            return transpositionLookup;
        }

        List<Move> moves = moveOrderer.getOrderedMoves(board, null, killerMoves, depth);

        if(moves.isEmpty()){
            if(board.isKingAttacked()) {
                return -mate;
            }

            return boardEvaluator.calculateContemptFactor(board);
        }

        Flag flag = Flag.UPPERBOUND;
        boolean pvNode = false;

        for(Move move : moves) {
            board.doMove(move);
            int extension = calculateSearchExtension(move, totalExtensions, board);
            int score;

            if(pvNode) {
                score = -negamax(depth - 1 + extension, -alpha - 1, -alpha, totalExtensions + extension, mate - 1, board, tTable, searchStartTime, killerMoves);
                if(score > alpha && score < beta) score = -negamax(depth - 1 + extension, -beta, -alpha, totalExtensions + extension, mate - 1, board, tTable, searchStartTime, killerMoves);
            } else {
                score = -negamax(depth - 1 + extension, -beta, -alpha, totalExtensions + extension, mate - 1, board, tTable, searchStartTime, killerMoves);
            }
            
            board.undoMove();

            if(maxSearchTimeExceeded(searchStartTime)) {
                return INFINITY;
            }

            if(score >= beta) {
                tTable.setEntry(board, Flag.LOWERBOUND, beta, depth, hashKey);
                killerMoves.storeKillerMove(move, depth, board);
                return beta;
            }

            if(score > alpha) {
                flag = Flag.EXACT;
                alpha = score;
                pvNode = true;
            }
        }

        tTable.setEntry(board, flag, alpha, depth, hashKey);
        return alpha;
    }

    private boolean maxSearchTimeExceeded(long searchStartTime) {
        return System.currentTimeMillis() - searchStartTime > TIME_TO_THINK;
    }

    private int calculateSearchExtension(Move move, int totalExtensions, Board board) {
        int extension = 0;

        if(totalExtensions < MAX_NUMBER_EXTENSION){
            if(board.isKingAttacked()) extension = 1;
        }

        return extension;
    }

    private int quiescenceSearch(int alpha, int beta, Board board) {
        int standPat = boardEvaluator.evaluate(board);

        if(standPat >= beta) {
            return beta;
        }

        alpha = Math.max(alpha, standPat);

        for(Move capture : moveOrderer.getCaptures(board, null, true)) {
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
