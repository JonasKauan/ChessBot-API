package com.zika.chessbot.controller;

import com.zika.chessbot.bot.ChessBot;
import com.zika.chessbot.bot.utils.BishopUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/bot")
@RequiredArgsConstructor
public class ChessBotController {
    private final ChessBot chessBot;

    @GetMapping(value = "/movimento")
    public Map<String,String> calcularMovimento(@RequestParam String fenString) {
        return Map.of("move", chessBot.decideMove(fenString));
    }
}
