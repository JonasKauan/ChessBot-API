package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.move.Move;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TranspositionEntry {
    private long key;
    private Flag flag;
    private Move move;
    private double score;
    private int depth;

    public TranspositionEntry(long key, Flag flag, double score, int depth) {
        this.flag = flag;
        this.score = score;
        this.depth = depth;
    }
}
