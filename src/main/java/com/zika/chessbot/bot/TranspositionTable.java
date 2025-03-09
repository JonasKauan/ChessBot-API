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
    private static final long TABLE_SIZE = 64 * 1024 * 1024;

    public TranspositionTable() {
        this.table = new HashMap<>();
    }

    public TranspositionEntry retrieveEntry(long hashKey, int depth) {
        if(!table.containsKey(hashKey % TABLE_SIZE)) {
            return null;
        }

        TranspositionEntry entry = table.get(hashKey % TABLE_SIZE);

        if(entry.key() == hashKey && entry.depth() >= depth) {
            return entry;
        }

        return null;
    }

    public void setEntry(long hashKey, Flag flag, Move move, int score, int depth) {
        table.put(hashKey % TABLE_SIZE, new TranspositionEntry(hashKey, flag, move, score, depth));
    }
}
