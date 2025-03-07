package com.zika.chessbot.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Square;
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
    private final ZorbristHash zorbrist;

    public Move getOpeningMove(Board board) {
        List<BookEntry> entries = new ArrayList<>();
        long hashCode = zorbrist.generateHash(board);

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

        return new Move(
                squares[getSquareIndex(from)],
                squares[getSquareIndex(to)],
                parsePromotion(promotion, board)
        );
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

    private int getSquareIndex(int square) {
        return (square % 8) + 8 * (square / 8);
    }
}
