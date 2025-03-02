package com.zika.chessbot.bot.utils;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.zika.chessbot.bot.BoardUtils;
import org.springframework.stereotype.Service;

@Service
public class KingUtils {

    public int countAttackersNearKingZone(Square kingLocation, Board board) {
        int[] kingZone = kingZone(kingLocation);
        Square[] squares = Square.values();
        int attackers = 0;

        for(int squareIndex : kingZone) {
            if(squareIndex >= squares.length || squareIndex < 0) {
                continue;
            }

            Square square = squares[squareIndex];
            attackers += BoardUtils.countMovesToSquare(board, square);
        }

        return attackers;
    }

    private int[] kingZone(Square kingSquare) {
        int kingSquareIndex = kingSquare.ordinal();

        return new int[] {
            kingSquareIndex + 7, kingSquareIndex + 8, kingSquareIndex + 9,
            kingSquareIndex - 1, kingSquareIndex, kingSquareIndex + 1,
            kingSquareIndex - 9, kingSquareIndex - 8, kingSquareIndex - 7
        };
    }

}
