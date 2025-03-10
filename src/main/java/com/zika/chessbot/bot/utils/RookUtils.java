package com.zika.chessbot.bot.utils;

import com.github.bhlangonijr.chesslib.*;
import com.zika.chessbot.bot.BoardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RookUtils {
    private final BitBoardUtils bitBoardUtils;

    public double getFileOpenness(Square square, Board board) {
        Side pieceSide = board.getPiece(square).getPieceSide();
        String opSideFenChar = pieceSide == Side.WHITE ? "p" : "P";

        long rookFileMask = bitBoardUtils.getRookOpenFileMask(square, pieceSide);
        long enemyPawnBitBoard = board.getBitboard(Piece.fromFenSymbol(opSideFenChar));
        long friendlyPieceBitBoard = board.getBitboard(pieceSide);

        if((rookFileMask & friendlyPieceBitBoard) != 0) {
            return 0.;
        }

        if((rookFileMask & enemyPawnBitBoard) != 0) {
            return .5;
        }

        return 1.;
    }
}
