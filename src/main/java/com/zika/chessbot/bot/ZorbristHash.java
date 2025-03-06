package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.*;
import com.zika.chessbot.bot.utils.ZobristUtils;
import org.springframework.stereotype.Service;


@Service
public class ZorbristHash {
    public long generateHash(Board board) {
        long hash = 0;

        for(Square square : Square.values()) {
            Piece piece = board.getPiece(square);

            if(piece == Piece.NONE) {
                continue;
            }

            int fileIndex = square.getFile().ordinal();
            int rankIndex = square.getRank().ordinal();

            hash ^= ZobristUtils.zobristTable[64 * pieceIndex(piece) + 8 * rankIndex + fileIndex];
        }

        int[] castleIndex = castlingRightsIndex(board);

        if(castleIndex != null) {
            for(int index : castleIndex) {
                hash ^= ZobristUtils.zobristTable[768 + index];
            }
        }

        Square enPassantSquare = board.getEnPassant();

        if(enPassantSquare != Square.NONE && isEnPassantValid(board, enPassantSquare)) {
            hash ^= ZobristUtils.zobristTable[772 + enPassantSquare.getFile().ordinal()];
        }

        if(board.getSideToMove() == Side.WHITE) {
            hash ^= ZobristUtils.zobristTable[780];
        }

        return hash;
    }

    private int[] castlingRightsIndex(Board board) {
        CastleRight whiteCastleRight = board.getCastleRight(Side.WHITE);
        CastleRight blackCastleRight = board.getCastleRight(Side.BLACK);

        if(
            whiteCastleRight == CastleRight.NONE
            && blackCastleRight == CastleRight.NONE
        ) {
            return null;
        }

        int[] castleIndexesWhite = switch(whiteCastleRight) {
            case KING_SIDE -> new int[] { 0 };
            case QUEEN_SIDE, KING_AND_QUEEN_SIDE -> new int[] { 0, 1 };
            default -> new int[]{};
        };

        int[] castleIndexesBlack = switch(blackCastleRight) {
            case KING_SIDE -> new int[] { 2 };
            case QUEEN_SIDE, KING_AND_QUEEN_SIDE -> new int[] { 2, 3 };
            default -> new int[]{};
        };

        int[] castleIndexes = new int[castleIndexesWhite.length + castleIndexesBlack.length];

        System.arraycopy(castleIndexesWhite, 0, castleIndexes, 0, castleIndexesWhite.length);
        System.arraycopy(castleIndexesBlack, 0, castleIndexes, castleIndexesWhite.length, castleIndexesBlack.length);

        return castleIndexes;
    }

    private boolean isEnPassantValid(Board board, Square enPassantSquare) {
        Square[] squares = Square.values();

        Square enPassantIColumn = squares[enPassantSquare.ordinal() + 1];
        Square enPassantJColumn = squares[enPassantSquare.ordinal() - 1];

        if(enPassantIColumn.getRank() != enPassantSquare.getRank()) {
            return board.getPiece(enPassantJColumn).getPieceType() == PieceType.PAWN;
        }

        if(enPassantJColumn.getRank() != enPassantSquare.getRank()) {
            return board.getPiece(enPassantIColumn).getPieceType() == PieceType.PAWN;
        }

        return board.getPiece(enPassantJColumn).getPieceType() == PieceType.PAWN
                || board.getPiece(enPassantIColumn).getPieceType() == PieceType.PAWN;
    }

    private int pieceIndex(Piece piece){
        return switch(piece) {
            case BLACK_PAWN -> 0;
            case WHITE_PAWN -> 1;
            case BLACK_KNIGHT -> 2;
            case WHITE_KNIGHT -> 3;
            case BLACK_BISHOP -> 4;
            case WHITE_BISHOP -> 5;
            case BLACK_ROOK -> 6;
            case WHITE_ROOK -> 7;
            case BLACK_QUEEN -> 8;
            case WHITE_QUEEN -> 9;
            case BLACK_KING -> 10;
            case WHITE_KING -> 11;
            default -> -1;
        };
    }
}
