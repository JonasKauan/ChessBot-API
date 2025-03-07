package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.move.Move;

import lombok.Getter;
import lombok.Setter;

public record TranspositionEntry(
    long key,
    Flag flag,
    Move move,
    int score,
    int depth
) {}
