package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpeningBook {
    private final PolyglotBook book;
    private final ZorbristHash zorbristHasher;

    public Move getOpeningMove(Board board) {
        List<BookEntry> entries = new ArrayList<>();
        long hashCode = zorbristHasher.generateHash(board);

        for(BookEntry entry : book.getEntries()) {
            if(entry.key() == hashCode) {
                entries.add(entry);
            }
        }

        if(entries.isEmpty()) {
            log.info("[OpeningBook.getOpeningMove] Não foi encontrada entrada no opening book para a posição");
            return null;
        }

        Move move = parsePolyglotMove(weightedRandomMove(entries), board);
        log.info("[OpeningBook.getOpeningMove] Encontrada entrada no livro de aberturas: {}", move);
        return move;
    }

    private int weightedRandomMove(List<BookEntry> entries) {
        int totalWeight = entries.stream().mapToInt(BookEntry::weight).sum();
        int randomWeight = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulativeWeight = 0;

        for(BookEntry entry : entries) {
            cumulativeWeight += entry.weight();

            if(randomWeight < cumulativeWeight) {
                return entry.move();
            }
        }

        return entries.get(0).move();
    }

    private Move parsePolyglotMove(int bitMove, Board board) {
        int from  = (bitMove >> 6) & 0x3F;
        int to = bitMove & 0x3F;
        int promotion = (bitMove >> 12) & 0xF;

        Square[] squares = Square.values();

        return isCatlingMove(squares[from], squares[to], board)
            ? handleCastling(squares[from], squares[to])
            : new Move(squares[from], squares[to], parsePromotion(promotion, board));
    }

    private Piece parsePromotion(int promotion, Board board) {
        PieceType type = switch(promotion) {
            case 1 -> PieceType.KNIGHT;
            case 2 -> PieceType.BISHOP;
            case 3 -> PieceType.ROOK;
            case 4 -> PieceType.QUEEN;
            default -> null;
        };

        if(type == null) {
            return Piece.NONE;
        }

        return Piece.fromValue(board.getSideToMove()+"_"+type);
    }

    private boolean isCatlingMove(Square from, Square to, Board board) {
        Piece piece = board.getPiece(from);

        if(piece.getPieceType() != PieceType.KING) {
            return false;
        }

        if(piece.getPieceSide() == Side.WHITE) {
            return from == Square.E1 && (to == Square.H1 || to == Square.A1);
        }

        return from == Square.E8 && (to == Square.H8 || to == Square.A8);
    }

    private Move handleCastling(Square from , Square to) {
        Square castlingSquare = switch(to) {
            case H1 -> Square.G1;
            case A1 -> Square.C1;
            case H8 -> Square.G8;
            case A8 -> Square.C8;
            default -> to;
        };

        return new Move(from, castlingSquare);
    }
}
