package com.zika.chessbot.bot;

import java.util.Map;

import java.util.HashMap;

import com.github.bhlangonijr.chesslib.Board;

public class TranspositionTable {
    private final Map<Long, TranspositionEntry> map;
    private final long tableSize;
    private static final int LOOKUP_FAILED = Integer.MIN_VALUE;

    public TranspositionTable(){
        this.map = new HashMap<>();
        this.tableSize = 1024 * 1024 * 1024;
    }

    public int lookupEvaluation(Board board, int depth, int alpha, int beta){
        long hashKey = ZorbristHash.generateHash(board);
 
        TranspositionEntry entry = this.getEntry(hashKey % this.tableSize);
        
        if(this.hasEntry(hashKey % this.tableSize) && entry.getKey() == hashKey){
            if(entry.getDepth() >= depth){
                if(entry.getFlag() == Flag.EXACT) return entry.getScore();
                if(entry.getFlag() == Flag.UPPERBOUND && entry.getScore() <= alpha) return alpha;
                if(entry.getFlag() == Flag.UPPERBOUND && entry.getScore() >= beta) return beta;
            }
        }
        
        return LOOKUP_FAILED;
    }

    public void setEntry(Board board, Flag flag, int score, int depth){
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
}
