package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import org.springframework.stereotype.Service;

@Service
public class BoardEvaluator {

    public int evaluate(Board board) {
        int evaluation = calculateScore(board, Side.WHITE) - calculateScore(board, Side.BLACK);
        return evaluation * (board.getSideToMove() == Side.WHITE ? 1 : -1);
    }

    public double calculateContemptFactor(Board board){
        double contempt = -.5;
        int evaluation = evaluate(board);
        return contempt * calculateEndGameWeigth(board) * (evaluation > 0 ? 1 : -1);
    }

    public double calculateEndGameWeigth(Board board){
        long bitBoard = board.getBitboard();
        int numberPieces = 0;

        for(int i = 0; i < 64; i++){
            long bit = 1L << i;
            if((bitBoard & bit) == 0) continue;
            numberPieces++;
        }

        return numberPieces / 32.;
    }

    private int calculateScore(Board board, Side side) {
        PieceType[] pieces = PieceType.values();
        
        int score = 0;
        for(PieceType type : pieces){
            if(type == PieceType.NONE) continue;    
            score += quantifyPieces(board, type, side);
        }

        return score;
    }

    private int quantifyPieces(Board board, PieceType type, Side side) {
        long pieceBitBoard = board.getBitboard(Piece.valueOf(side.toString()+"_"+type.toString())) & board.getBitboard();

        int piecesCount = 0;
        Weights w = Weights.valueOf(side.toString()+"_"+type.toString());

        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                long bit = 1L << (i * 8 + j);
                if((pieceBitBoard & bit) == 0) continue;
                piecesCount += w.getWeight() + w.getPositionWeight()[i][j];
            }
        }

        return piecesCount;
    }
}
