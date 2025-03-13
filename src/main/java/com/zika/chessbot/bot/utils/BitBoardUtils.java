package com.zika.chessbot.bot.utils;

import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import org.springframework.stereotype.Service;

@Service
public class BitBoardUtils {
    private static final long A_FILE_BIT_BOARD = 0X0101010101010101L;

    public long getPassedPawnMask(Square square, Side side) {
        long columnMask = A_FILE_BIT_BOARD << square.getFile().ordinal()
                | A_FILE_BIT_BOARD << Math.max(0, square.getFile().ordinal() - 1)
                | A_FILE_BIT_BOARD << Math.min(7, square.getFile().ordinal() + 1);

        return getFowardMask(square, side) & columnMask;
    }

    public long getRookOpenFileMask(Square square, Side side) {
        long columnMask = A_FILE_BIT_BOARD << square.getFile().ordinal();
        return getFowardMask(square, side) & columnMask;
    }

    public long getDiagonalMask(Square square) {
        long squareMask = 1L << square.ordinal();
        long diagonalMask = getMainDiagonalMask(squareMask, square) | getSecondaryDiagonalMask(squareMask, square);
        return squareMask ^ diagonalMask;
    }

    private long getSecondaryDiagonalMask(long squareMask, Square square) {
        for(int i = 1; i < Math.min(square.getRank().ordinal(), 7 - square.getFile().ordinal()); i++) {
            squareMask |= squareMask >> 7 * i & 0xFEFEFEFEFEFEFEFEL;
        }

        for(int i = 1; i < Math.min(7 - square.getRank().ordinal(), square.getFile().ordinal()); i++) {
            squareMask |= squareMask << 7 * i & 0x7F7F7F7F7F7F7F7FL;
        }

        return squareMask;
    }

    private long getMainDiagonalMask(long squareMask, Square square) {
        for(int i = 1; i < Math.min(square.getRank().ordinal(), square.getFile().ordinal()); i++) {
            squareMask |= squareMask >> 9 * i & 0x7F7F7F7F7F7F7F7FL;
        }

        for(int i = 1; i < Math.min(7 - square.getRank().ordinal(), 7 - square.getFile().ordinal()); i++) {
            squareMask |= squareMask << 9 * i & 0xFEFEFEFEFEFEFEFEL;
        }

        return squareMask;
    }

    private long getFowardMask(Square square, Side side) {
        if(side == Side.WHITE) {
            return (-1L << 8 * (square.getRank().ordinal() + 1));
        }

        int rankIndex = 8 - square.getRank().ordinal();

        return (-1L >>> 8 * rankIndex);
    }
}
