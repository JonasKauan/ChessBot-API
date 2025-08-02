package com.zika.chessbot.bot;

import java.util.ArrayList;
import java.util.List;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;
import com.zika.chessbot.bot.utils.BoardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// TODO: Ele parece não estar achando o mate do jeito mais rápido, fazer algo a respeito
// TODO: Aprimorar endgame
// TODO: Aprimorar velocidade na busca
// TODO: Arrumar extensões
// TODO: Melhorar avaliação do tabuleiro
// TODO: Ver essa porra de PV

@Slf4j
@Service
@RequiredArgsConstructor
public class ChessBot {
    private final BoardEvaluator boardEvaluator;
    private final SanParser sanParser;
    private final MoveOrderer moveOrderer;
    private final OpeningBook openingBook;
    private final ZorbristHash zorbristHasher;
    private final TranspositionTable tTable;
    private static final int MAX_NUMBER_EXTENSION = 16;
    private static final int MATE_SCORE = 100_000;
    private static final int INFINITY = MATE_SCORE * 2;
    private static final long TIME_TO_THINK = 60_000;
    private static final int VAL_WINDOW = 50;

    public String decideMove(String fenString, boolean enableLogs) {
        Board board = new Board();
        board.loadFromFen(fenString);
        Move openingMove = openingBook.getOpeningMove(board, enableLogs);
        return openingMove == null ? search(board, enableLogs) : sanParser.parseToSan(board, openingMove);
    }

    private String search(Board board, boolean enableLogs) {
        int alpha = -INFINITY;
        int beta = INFINITY;

        KillerMoves killerMoves = new KillerMoves();
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        long searchStartTime = System.currentTimeMillis();

        for(int depth = 1; ; depth++) {
            //System.out.println(depth);
            int aspirationWindow = VAL_WINDOW;

            boolean aspirationWindowAdjusted = false;
            int failCount = 0;

            while(!aspirationWindowAdjusted) {
                if(failCount > 1) {
                    alpha = -INFINITY;
                    beta = INFINITY;
                }

                for(Move move : moveOrderer.getOrderedMoves(board, bestMove, killerMoves, null)) {
                    board.doMove(move);
                    double halfPlyDepth = depth * 2 - 2;
                    int score = -negamax(halfPlyDepth, alpha, beta, 0, board, killerMoves);
                    board.undoMove();

                    if(score > bestScore) {
//                        System.out.println("Move: " + move);
//                        System.out.println("Score: " + score);
                        bestMove = move;
                        bestScore = score;
                    }

                    if(System.currentTimeMillis() - searchStartTime >= TIME_TO_THINK) {
                        String parsedMove = sanParser.parseToSan(board, bestMove);

                        if(enableLogs) {
                            log.info("Busca terminada após profundidade de {}", depth);
                            log.info("Melhor movimento encontrado {}", parsedMove);
                        }

                        return parsedMove;
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
            Board board,
            KillerMoves killerMoves
    ) {
        if(halfPlyDepth <= 0) {
            return quiescenceSearch(alpha, beta, board);
        }

        int fullPlyDepth = (int) (halfPlyDepth / 2);
        long hashKey = zorbristHasher.generateHash(board);

        TranspositionEntry entry = tTable.retrieveEntry(hashKey, fullPlyDepth);

        if(entry != null) {
            if(entry.flag() == Flag.EXACT) return entry.score();
            if(entry.flag() == Flag.UPPERBOUND && entry.score() <= alpha) return alpha;
            if(entry.flag() == Flag.UPPERBOUND && entry.score() >= beta) return beta;
        }

        if(board.isMated()) {
            //System.out.println("Estou chegando no mate " + fullPlyDepth);
            return (int) -(MATE_SCORE - halfPlyDepth);
        }

        Move tableBestMove = entry != null ? entry.move() : null;
        List<Move> moves = moveOrderer.getOrderedMoves(board, tableBestMove, killerMoves, fullPlyDepth);

        if(moves.isEmpty()) {
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
                score = -negamax(actualDepth, -alpha - 1, -alpha, totalExtensions, board, killerMoves);

                if(score > alpha && score < beta)
                    score = -negamax(actualDepth, -beta, -alpha, totalExtensions, board, killerMoves);
            } else {
                score = -negamax(actualDepth, -beta, -alpha, totalExtensions, board, killerMoves);
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
            // TODO: Fazer essas extenções de um jeito mais inteligente
//            if(board.isKingAttacked()) {
//                extension = 1;
//            }

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
