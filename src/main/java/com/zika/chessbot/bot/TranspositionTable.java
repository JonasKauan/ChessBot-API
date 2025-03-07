package com.zika.chessbot.bot;

import java.util.Map;

import java.util.HashMap;

import com.github.bhlangonijr.chesslib.move.Move;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TranspositionTable {
    private final Map<Long, TranspositionEntry> table;
    private final long tableSize;
    private static final int LOOKUP_FAILED = Integer.MIN_VALUE;

    public TranspositionTable(){
        this.table = new HashMap<>();
        this.tableSize = 64 * 1024 * 1024;
    }

    public int lookupEvaluation(int depth, int alpha, int beta, long hashKey) {
        long key = hashKey % tableSize;

        if(table.containsKey(key)) {
            TranspositionEntry entry = table.get(key);

            if(entry.key() == hashKey && entry.depth() >= depth) {
                if(entry.flag() == Flag.EXACT) return entry.score();
                if(entry.flag() == Flag.UPPERBOUND && entry.score() <= alpha) return alpha;
                if(entry.flag() == Flag.UPPERBOUND && entry.score() >= beta) return beta;
            }
        }
        
        return LOOKUP_FAILED;
    }

    public void setEntry(long hashKey, Flag flag, Move move, int score, int depth) {
        this.table.put(hashKey % tableSize, new TranspositionEntry(hashKey, flag, move, score, depth));
    }
}
