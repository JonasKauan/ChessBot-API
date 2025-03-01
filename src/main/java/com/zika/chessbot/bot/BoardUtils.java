package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;

public class BoardUtils {
    public static boolean captureMove(Move move, Board board) {
        return board.getPiece(move.getTo()) != Piece.NONE;
    }
}
