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

    private long getFowardMask(Square square, Side side) {
        if(side == Side.WHITE) {
            return (-1L << 8 * (square.getRank().ordinal() + 1));
        }

        int rankIndex = 8 - square.getRank().ordinal();

        return (-1L >>> 8 * rankIndex);
    }
}
