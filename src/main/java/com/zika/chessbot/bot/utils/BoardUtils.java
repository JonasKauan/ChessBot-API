package com.zika.chessbot.bot.utils;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.zika.chessbot.bot.Weights;

import java.util.List;

public class BoardUtils {

    public static boolean isCaptureMove(Move move, Board board) {
        return board.getPiece(move.getTo()) != Piece.NONE;
    }

    public static boolean isMateMove(Board board, Move move) {
        board.doMove(move);
        boolean isMate = board.isMated();
        board.undoMove();
        return isMate;
    }

    public static int getDistanceToEndBoard(Square square, Side side) {
        if(side == Side.WHITE) {
            return 7 - square.getRank().ordinal();
        }

        return square.getRank().ordinal();
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
