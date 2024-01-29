package com.zika.chessbot.classes;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;

public class ZorbristHash {
    private static long[][][] zTable = setZtable();

    public static long generateHash(Board board){
        Piece[][] boardMatrix = getBoardMatrix(board);
        long hash = 0;
        
        for(int i = 0; i < boardMatrix.length; i++){
            for(int j = 0; j < boardMatrix[i].length; j++){
                if(boardMatrix[i][j] == null) continue;
                hash ^= zTable[i][j][getPieceIndex(boardMatrix[i][j])];
            }
        }

        return hash;
    }


    private static int getPieceIndex(Piece piece){
        switch (piece) {
            case WHITE_PAWN -> {return 0;}
            case WHITE_KNIGHT -> {return 1;}
            case WHITE_BISHOP -> {return 2;}
            case WHITE_ROOK -> {return 3;}
            case WHITE_QUEEN -> {return 4;}
            case WHITE_KING -> {return 5;}
            case BLACK_PAWN -> {return 6;}
            case BLACK_KNIGHT -> {return 7;}
            case BLACK_BISHOP -> {return 8;}
            case BLACK_ROOK -> {return 9;}
            case BLACK_QUEEN -> {return 10;}
            case BLACK_KING -> {return 11;}
            default -> {return -1;}
        }
    }

    private static Piece[][] getBoardMatrix(Board board){
        Piece[][] boardMatrix = new Piece[8][8];
        Piece[] pieces = Piece.values();
        for(Piece piece : pieces){
            if(piece == Piece.NONE) continue;
            long pieceBitBoard = board.getBitboard(piece) & board.getBitboard();
            for(int i = 0; i < 8; i++){
                for(int j = 0; j < 8; j++){
                    long bit = 1L << (i * 8 + j);
                    if((pieceBitBoard & bit) == 0) continue;
                    boardMatrix[i][j] = piece;
                }
            }
        }

        return boardMatrix;
    }

    private static long[][][] setZtable(){
        long[][][] table = new long[8][8][12];
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                for(int k = 0; k < 12; k++){
                    table[i][j][k] = (long)(Math.floor(Math.random() * Math.pow(2, 64)));
                }
            }
        }

        return table;
    }
}
