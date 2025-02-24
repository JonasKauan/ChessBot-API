package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.CastleRight;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import org.springframework.stereotype.Service;

@Service
public class SanParser {
    public String parseToSan(Board board, Move move){
        if(move == null) return "null";
        StringBuilder san = new StringBuilder();
        Piece piece = board.getPiece(move.getFrom());
        Piece capturedPiece = board.getPiece(move.getTo());

        if( piece.getSanSymbol().equals("K") &&
            (board.getCastleRight(Side.BLACK) != CastleRight.NONE ||
            board.getCastleRight(Side.WHITE) != CastleRight.NONE)
        ){
            if(move.getTo().toString().equals("G1") || move.getTo().toString().equals("G8")) return "O-O";
    
            if(move.getTo().toString().equals("B1") || move.getTo().toString().equals("B8")) return "O-O-O";
        }
        
        boolean duplicatePiece = board.legalMoves()
                                    .stream()
                                    .filter(m -> !m.toString().equals(move.toString()))
                                    .filter(m -> board.getPiece(m.getFrom()) == piece)
                                    .anyMatch(m -> m.getTo() == move.getTo());
                                    
        if(!piece.getSanSymbol().isEmpty()) san.append(piece.getSanSymbol());

        if(duplicatePiece) san.append(move.getFrom().getFile().getNotation().toLowerCase());
        
        if(capturedPiece.getPieceType() != null){
            if(piece.getPieceType() == PieceType.PAWN && !duplicatePiece){
                san.append(move.getFrom().toString().toLowerCase());
            }
            san.append("x");
        }
        
        san.append(move.getTo().toString().toLowerCase());

        board.doMove(move);
        if(board.isMated()) san.append("#");
        else if(board.isKingAttacked()) san.append("+");
        board.undoMove();

        return san.toString();
    }

}
