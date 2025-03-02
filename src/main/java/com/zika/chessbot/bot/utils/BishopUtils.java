package com.zika.chessbot.bot.utils;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import org.springframework.stereotype.Service;

@Service
public class BishopUtils {

    private final static int[] DIAGONAL_DIRECTIONS = {-9, -7, 7, 9};

    public int countDiagonalMoves(Board board, Square square) {
        int mobility = 0;
        Square[] squares = Square.values();

        for(int direction : DIAGONAL_DIRECTIONS) {
            int actualPosition = square.ordinal() + direction;

            if(
                actualPosition >= squares.length
                || actualPosition < 0
                || square.getRank() == squares[actualPosition].getRank()
            ) {
                continue;
            }

            Square nextSquare = squares[actualPosition];

            while(board.getPiece(nextSquare) == Piece.NONE) {
                mobility++;
                actualPosition = nextSquare.ordinal() + direction;

                if(
                    actualPosition >= squares.length
                    || actualPosition < 0
                    || nextSquare.getRank() == squares[actualPosition].getRank()
                ) {
                    break;
                }

                nextSquare = squares[actualPosition];
            }
        }

        return mobility;
    }

    public int countBlockedDiagonals(Board board, Square square) {
        int blockedDiagonals = 0;
        Square[] squares = Square.values();

        for(int direction : DIAGONAL_DIRECTIONS) {
            int actualPosition = square.ordinal() + direction;

            if(actualPosition >= squares.length || actualPosition < 0) {
                continue;
            }

            Square nextSquare = squares[actualPosition];

            if(nextSquare.getRank() == square.getRank()) {
                continue;
            }

            while(actualPosition < squares.length && actualPosition >= 0) {
                actualPosition = nextSquare.ordinal() + direction;

                if(
                    actualPosition >= squares.length
                    || actualPosition < 0
                    || nextSquare.getRank() == squares[actualPosition].getRank()
                ) {
                    break;
                }

                nextSquare = squares[actualPosition];

                if(board.getPiece(nextSquare) != Piece.NONE) {
                    blockedDiagonals++;
                }
            }
        }


        return blockedDiagonals;
    }
}
