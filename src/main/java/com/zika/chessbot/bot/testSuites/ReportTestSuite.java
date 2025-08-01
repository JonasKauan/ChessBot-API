package com.zika.chessbot.bot.testSuites;

import java.util.List;

public record ReportTestSuite(
    String precisao,
    List<Case> cenariosFalhados,
    List<Case> cenariosSucesso
) {}
