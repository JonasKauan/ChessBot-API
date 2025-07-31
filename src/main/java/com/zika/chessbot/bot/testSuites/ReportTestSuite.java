package com.zika.chessbot.bot.testSuites;

import java.util.List;

public record ReportTestSuite(
    List<Case> cenariosFalhados,
    List<Case> cenariosSucesso,
    String precisao
) {}
