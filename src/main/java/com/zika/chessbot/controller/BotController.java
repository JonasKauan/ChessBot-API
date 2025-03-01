package com.zika.chessbot.controller;

import com.zika.chessbot.bot.ChessBot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@CrossOrigin
@RestController
@RequestMapping("/bot")
@RequiredArgsConstructor
public class BotController {
    private final ChessBot chessBot;

    @GetMapping(value = "/movimento")
    public Map<String,String> calcularMovimento(@RequestParam String fen) {
        return Map.of("move", chessBot.decide(fen));
    }
}
