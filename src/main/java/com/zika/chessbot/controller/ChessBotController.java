package com.zika.chessbot.controller;

import com.github.bhlangonijr.chesslib.Board;
import com.zika.chessbot.bot.BoardEvaluator;
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
    private final BoardEvaluator evaluator;

    @GetMapping("/movimento")
    public Map<String,String> calcularMovimento(@RequestParam String fenString) {
        return Map.of("move", chessBot.decideMove(fenString, true));
    }

    @GetMapping("/test-suites")
    public ReportTestSuite rodarSuitesTeste() {
        return testSuiteService.rodarSuitesTeste();
    }

    @GetMapping("/teste")
    public Integer fodase(@RequestParam String fen) {
        Board board = new Board();
        board.loadFromFen(fen);
        return evaluator.evaluate(board);
    }
}
