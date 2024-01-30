package com.zika.chessbot.classes;

import java.util.List;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

// TODO Implementar Killer MOoves
// TODO Implementar Aspiration window
// TODO Implementar Opening book

public class Search {
    private final int INFINITY = 9999999;
    private final int lookupFailed = Integer.MIN_VALUE;
    private final int maxNumberExtension = 16;
    private final int mateScore = Integer.MAX_VALUE;
    private final long timeToThink = 5000;
    private final int valWindow = 50;
    private long searchStartTime;
    private TranspositionTable tTable;
    private int finalDepth;
    private Board board;

    public Search(String fenStr, int depth){
        this.board = new Board();
        this.board.loadFromFen(fenStr);
        this.tTable = new TranspositionTable();
        this.finalDepth = depth;
    }

    public String decide(){
        double alpha = -INFINITY;
        double beta = INFINITY;
        Move bestMove = null;        

        for(int depth = 1; depth <= this.finalDepth; depth++){
            // System.out.println("Depth "+depth);
            double bestScore = -INFINITY;

            for(Move move : MoveOrderer.getOrderedMoves(board, bestMove)){
                this.board.doMove(move);
                double score = -negamax(depth - 1, alpha, beta, 0, mateScore);
                this.board.undoMove();

                if(score >= bestScore){
                    bestMove = move;
                    bestScore = score;
                }

                // System.out.println(Parser.parseToSan(this.board, move)+" "+score);
            }

            // System.out.println("\nBest Move: "+Parser.parseToSan(board, bestMove)+"\n");
        }

        return Parser.parseToSan(this.board, bestMove);
    }

    public boolean timesUp(){
        return (System.currentTimeMillis() - this.searchStartTime) >= this.timeToThink;
    }

    public double negamax(int depth, double alpha, double beta, int totalExtensions, int mate){
        if(depth == 0) return quiescenceSearch(alpha, beta);

        double transpositionLookup = tTable.lookupEvaluation(this.board, depth, alpha, beta);

        if(transpositionLookup != lookupFailed) return transpositionLookup;

        List<Move> moves = MoveOrderer.getOrderedMoves(board, null);

        if(moves.isEmpty()){
            if(this.board.isKingAttacked()) return -mate;
            return calculateContemptFactor();
        }

        Flag flag = Flag.UPPERBOUND;
        boolean pvNode = false;

        for(Move move : moves){
            this.board.doMove(move);
            int extension = calculateSearchExtension(move, totalExtensions);
            double score;

            if(pvNode){
                score = -negamax(depth - 1 + extension, -alpha - 1, -alpha, totalExtensions + extension, mate - 1);

                if(score > alpha && score < beta) score = -negamax(depth - 1 + extension, -beta, -alpha, totalExtensions + extension, mate - 1);
            }else{
                score = -negamax(depth - 1 + extension, -beta, -alpha, totalExtensions + extension, mate - 1);
            }
            
            this.board.undoMove();

            if(score >= beta) {
                tTable.setEntry(this.board, Flag.LOWERBOUND, beta, depth);
                return beta;
            }

            if(score > alpha){
                flag = Flag.EXACT;
                alpha = score;
                pvNode = true;
            }
        }

        tTable.setEntry(this.board, flag, alpha, depth);

        return alpha;
    }

    private int calculateSearchExtension(Move move, int totalExtensions){
        int extension = 0;

        if(totalExtensions < maxNumberExtension){
            if(this.board.isKingAttacked()) extension = 1;
        }

        return extension;
    }

    private double calculateContemptFactor(){
        double contempt = -.5;
        int evaluation = BoardEvaluator.evaluate(this.board);
        return contempt * calculateEndGameWeigth() * (evaluation > 0 ? 1 : -1);
    }

    public double calculateEndGameWeigth(){
        long bitBoard = this.board.getBitboard();
        int numberPieces = 0;
    
        for(int i = 0; i < 64; i++){
            long bit = 1L << i;
            if((bitBoard & bit) == 0) continue;
            numberPieces++;
        }

        return numberPieces / 32.;
    }

    // Se precisar em outro canto, tirar a lógica da calculateEndGameWeigth() e colocar aqui pra
    // Modularizar o código

    public int countPieces(){
        int numberPieces = 0;
        return numberPieces;
    }

    private double quiescenceSearch(double alpha, double beta){
        int standPat = BoardEvaluator.evaluate(this.board);
        if(standPat >= beta) return beta;
        alpha = Math.max(alpha, standPat);

        // TODO Gerar capturas boas ao inves de todas para melhorar a perfomace
        for(Move capture : MoveOrderer.getCaptures(this.board, null)){
            this.board.doMove(capture);
            double score = -quiescenceSearch(-beta, -alpha);
            this.board.undoMove();

            double delta = score - standPat;

            if(delta >= 0) return score;

            if(score >= beta) return beta;
            alpha = Math.max(alpha, score);
        }

        return alpha;
    }

    public Board getBoard(){
        return this.board;
    }
}
