package com.zika.chessbot;

import com.github.bhlangonijr.chesslib.move.Move;
import com.zika.chessbot.classes.Search;

public class Teste {
    public static void main(String[] args) {
        Search search = new Search("r1bqkb1r/pppp1ppp/2n2n2/4p1N1/2B1P3/8/PPPP1PPP/RNBQK2R b KQkq - 3 3", 5);

        // Search search = new Search("r1bqkb1r/ppppnBpp/5n2/4p1N1/4P3/8/PPPP1PPP/RNBQK2R b KQkq - 0 1", 3);

        // Search search = new Search("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w// KQkq - 0 1", 5);

        // System.out.println(search.decide());

        Move move = search.getBoard().legalMoves().get(0);
        System.out.println(move.getFrom().getFile().getNotation().toLowerCase());
        // System.out.println(search.calculateEndGameWeigth());
    }
}
