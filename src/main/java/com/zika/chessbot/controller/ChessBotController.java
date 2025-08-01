package com.zika.chessbot.controller;

import com.zika.chessbot.bot.ChessBot;
import com.zika.chessbot.bot.testSuites.ReportTestSuite;
import com.zika.chessbot.bot.testSuites.TestSuiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/bot")
@RequiredArgsConstructor
public class ChessBotController {
    private final ChessBot chessBot;
    private final TestSuiteService testSuiteService;

    @GetMapping("/movimento")
    public Map<String,String> calcularMovimento(@RequestParam String fenString) {
        return Map.of("move", chessBot.decideMove(fenString, true));
    }

    @GetMapping("/test-suites")
    public ReportTestSuite rodarSuitesTeste() {
        return testSuiteService.rodarSuitesTeste();
    }
}
