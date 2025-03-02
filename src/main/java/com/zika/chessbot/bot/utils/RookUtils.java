package com.zika.chessbot.bot.utils;

import com.github.bhlangonijr.chesslib.*;
import org.springframework.stereotype.Service;

@Service
public class RookUtils {

    public double getFileOpenness(Square square, Board board) {
        double fileOpenness = 1.;
        int direction = board.getSideToMove() == Side.WHITE ? 1 : -1;

        Square[] squares = Square.values();

        for(int i = 1; i < 8; i++) {
            int actualSquareIndex = square.ordinal() + 8 * i * direction;

            if(actualSquareIndex >= squares.length || actualSquareIndex < 0) {
                break;
            }

            Piece piece = board.getPiece(squares[actualSquareIndex]);

            if(piece != Piece.NONE) {
                if(piece.getPieceSide() == board.getSideToMove()) {
                    fileOpenness = 0.;
                    break;
                }

                if(piece.getPieceType() == PieceType.PAWN) {
                    fileOpenness = .5;
                }
            }
        }

        return fileOpenness;
    }
}
