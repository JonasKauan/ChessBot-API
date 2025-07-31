package com.zika.chessbot.bot.testSuites;

public record Case(
    String id,
    String position,
    String expected,
    String actual
) {

    public Case(EpdEntry epdEntry, String actual) {
        this(
                epdEntry.id(),
                epdEntry.fen(),
                epdEntry.bestMove(),
                actual
        );
    }

    public boolean success() {
        return expected.equals(actual);
    }

}
