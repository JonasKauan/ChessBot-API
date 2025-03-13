package com.zika.chessbot.bot.utils;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.zika.chessbot.bot.BoardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BishopUtils {
    private final BitBoardUtils bitBoardUtils;
    private final static int[] DIAGONAL_DIRECTIONS = { -9, -7, 7, 9 };

    public int countDiagonalMoves(Board board, Square square) {
        long bishopDiagonalsBitBoard = bitBoardUtils.getDiagonalMask(square) & board.getBitboard();
        int mobility = 0;

        for(int direction : DIAGONAL_DIRECTIONS) {
            int actualSquareIndex = square.ordinal() + direction;

            while(actualSquareIndex >= 0 && actualSquareIndex < 63) {
                if(actualSquareIndex / 8 == (actualSquareIndex - direction) / 8) {
                    break;
                }

                if((bishopDiagonalsBitBoard & (1L << actualSquareIndex)) != 0) {
                    break;
                }

                mobility++;
                actualSquareIndex += direction;
            }
        }

        return mobility;
    }

    public int countBlockedDiagonals(Board board, Square square) {
        long bishopDiagonalsBitBoard = bitBoardUtils.getDiagonalMask(square) & board.getBitboard();

        int blockedDiagonals = 0;

        for(int direction : DIAGONAL_DIRECTIONS) {
            int actualSquareIndex = square.ordinal() + direction;

            while(actualSquareIndex >= 0 && actualSquareIndex < 63) {
                if(actualSquareIndex / 8 == (actualSquareIndex - direction) / 8) {
                    break;
                }

                if((bishopDiagonalsBitBoard & (1L << actualSquareIndex)) != 0) {
                    blockedDiagonals++;
                    break;
                }

                actualSquareIndex += direction;
            }
        }

        return blockedDiagonals;
    }
}
