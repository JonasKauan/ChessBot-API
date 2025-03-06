package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.move.Move;

public record ScoredMove(Move move, Integer score) {}