package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.zika.chessbot.bot.utils.BoardUtils;

import java.util.HashMap;
import java.util.Map;

public class KillerMoves {
    private final Map<Integer, Move[]> moves;

    public KillerMoves() {
        this.moves = new HashMap<>();
    }

    public void storeKillerMove(Move move, int depth, Board board) {
        if(BoardUtils.captureMove(move, board)) {
            return;
        }

        if(!moves.containsKey(depth)) {
            moves.put(depth, new Move[2]);
            moves.get(depth)[0] = move;
            return;
        }

        if(moves.get(depth)[0] != move) {
            moves.get(depth)[1] = moves.get(depth)[0];
            moves.get(depth)[0] = move;
        }
    }

    public boolean isKillerMove(Move move, Integer depth) {
        if(depth == null || moves.isEmpty() || !moves.containsKey(depth)) {
            return false;
        }

        return (moves.get(depth)[0] != null && moves.get(depth)[0] == move)
                || (moves.get(depth)[1] != null && moves.get(depth)[1] == move);
    }
}
