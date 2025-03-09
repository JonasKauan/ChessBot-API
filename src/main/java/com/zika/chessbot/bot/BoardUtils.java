package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.List;

public class BoardUtils {

    public static final long A_FILE_BIT_BOARD = 0X0101010101010101L;

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

    public static void printBitBoard(long bitBoard) {
        for(int rank = 7; rank >= 0; rank--) {
            for(int file = 0; file < 8; file++) {
                long actualSquare = 1L << (rank * 8 + file);
                long bit = bitBoard & actualSquare;
                System.out.print((bit != 0 ? 1 : 0) + " ");
            }

            System.out.println();
        }

        System.out.println();
    }
}
