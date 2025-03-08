package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import org.springframework.stereotype.Service;

@Service
public class SanParser {
    public String parseToSan(Board board, Move move) {
        if(move == null) return "null";

        StringBuilder san = new StringBuilder();
        Piece piece = board.getPiece(move.getFrom());
        Piece capturedPiece = board.getPiece(move.getTo());

        if(moveIsCastling(board, move, piece)) {
            if(move.getTo() == Square.G1 || move.getTo() == Square.G8) return "O-O";
            if(move.getTo() == Square.C1 || move.getTo() == Square.C8) return "O-O-O";
        }
                                    
        if(!piece.getSanSymbol().isEmpty()) san.append(piece.getSanSymbol());

        boolean duplicatePiece = board.legalMoves()
                .stream()
                .filter(m -> m.getFrom() != move.getFrom())
                .filter(m -> board.getPiece(m.getFrom()) == piece)
                .anyMatch(m -> m.getTo() == move.getTo());

        if(duplicatePiece) san.append(move.getFrom().getFile().getNotation().toLowerCase());
        
        if(capturedPiece.getPieceType() != null){
            if(piece.getPieceType() == PieceType.PAWN && !duplicatePiece){
                san.append(move.getFrom().toString().toLowerCase());
            }
            san.append("x");
        }
        
        san.append(move.getTo().toString().toLowerCase());

        if(move.getPromotion() != Piece.NONE) {
            san.append(move.getPromotion().getSanSymbol().toUpperCase());
        }

        board.doMove(move);
        if(board.isMated()) san.append("#");
        else if(board.isKingAttacked()) san.append("+");
        board.undoMove();

        return san.toString();
    }

    private boolean moveIsCastling(Board board, Move move, Piece piece) {
        if(piece.getPieceType() != PieceType.KING) {
            return false;
        }

        Side kingSide = piece.getPieceSide();

        if(kingSide == Side.WHITE && move.getFrom() != Square.E1) {
            return false;
        }

        if(kingSide == Side.BLACK && move.getFrom() != Square.E8) {
            return false;
        }

        if(move.getTo() == Square.G1 || move.getTo() == Square.G8) {
            return board.getCastleRight(kingSide) == CastleRight.KING_SIDE
                    || board.getCastleRight(kingSide) == CastleRight.KING_AND_QUEEN_SIDE;
        }

        if(move.getTo() == Square.C1 || move.getTo() == Square.C8) {
            return board.getCastleRight(kingSide) == CastleRight.QUEEN_SIDE
                    || board.getCastleRight(kingSide) == CastleRight.KING_AND_QUEEN_SIDE;
        }

        return false;
    }

}
