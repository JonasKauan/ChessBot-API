package com.zika.chessbot.classes;

import java.util.Map;

import java.util.HashMap;

import com.github.bhlangonijr.chesslib.Board;

public class TranspositionTable {
    private Map<Long, TranspositionEntry> map;
    private final long tableSize = 1024 * 1024 * 1024; 


    public TranspositionTable(){
        this.map = new HashMap<>();
    }

    public double lookupEvaluation(Board board, int depth, double alpha, double beta){
        long hashKey = ZorbristHash.generateHash(board);
 
        TranspositionEntry entry = this.getEntry(hashKey % this.tableSize);
        
        if(this.hasEntry(hashKey % this.tableSize) && entry.getKey() == hashKey){
            if(entry.getDepth() >= depth){
                if(entry.getFlag() == Flag.EXACT) return entry.getScore();
                if(entry.getFlag() == Flag.UPPERBOUND && entry.getScore() <= alpha) return alpha;
                if(entry.getFlag() == Flag.UPPERBOUND && entry.getScore() >= beta) return beta;
            }
        }
        
        return Integer.MIN_VALUE;
    }

    public void setEntry(Board board, Flag flag, double score, int depth){
        long hashKey = ZorbristHash.generateHash(board);
        TranspositionEntry entry = new TranspositionEntry(hashKey, flag, score, depth);
        this.map.put(hashKey % this.tableSize, entry);
    }

    public TranspositionEntry getEntry(long hashCode){
        return this.map.get(hashCode);
    }

    public boolean hasEntry(long hashCode){
        return this.map.containsKey(hashCode);
    }

    public Map<Long, TranspositionEntry> getMap(){
        return this.map;
    }
}
