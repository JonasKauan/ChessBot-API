package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.List;

public class BoardUtils {

    public static boolean captureMove(Move move, Board board) {
        return board.getPiece(move.getTo()) != Piece.NONE;
    }

    public static boolean isSquareAttacked(Square square, Board board) {
        board.doNullMove();

        long attackCount = board.legalMoves()
                .stream()
                .filter(m -> m.getTo() == square)
                .count();

        board.undoMove();

        return attackCount > 0;
    }

    public static int countMovesToSquare(Board board, Square square) {
        board.doNullMove();

        List<Move> moves = board.pseudoLegalMoves().stream()
                .filter(move -> move.getTo() == square)
                .toList();

        board.undoMove();

        return moves.size();
    }

    public static boolean isEndGame(Board board) {
        int material = 0;
        Square[] squares = Square.values();

        for (Square square : squares) {
            Piece piece = board.getPiece(square);

            if(piece == Piece.NONE) {
                continue;
            }

            material += Weights.valueOf(piece.toString()).getPieceWeight();
        }

        return material <= 2400;
    }
}
