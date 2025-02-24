package com.zika.chessbot.controller;

import com.zika.chessbot.bot.ChessBot;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bot")
@RequiredArgsConstructor
public class BotController {
    private final ChessBot chessBot;

    @GetMapping(value = "/movimento")
    public String calcularMovimento(@RequestParam String fen) {
        return chessBot.decide(fen);
    }
}
