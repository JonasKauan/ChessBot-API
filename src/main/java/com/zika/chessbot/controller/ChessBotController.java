package com.zika.chessbot.controller;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.game.GameContext;
import com.zika.chessbot.bot.BoardUtils;
import com.zika.chessbot.bot.ChessBot;
import com.zika.chessbot.bot.utils.BishopUtils;
import com.zika.chessbot.bot.utils.BitBoardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/bot")
@RequiredArgsConstructor
public class ChessBotController {
    private final ChessBot chessBot;
    private final BishopUtils bishopUtils;

    @GetMapping(value = "/movimento")
    public Map<String,String> calcularMovimento(@RequestParam String fenString) {
        return Map.of("move", chessBot.decideMove(fenString));
    }

    @GetMapping(value = "/teste")
    public ResponseEntity<Void> teste() {
        bishopUtils.countBlockedDiagonals(new Board(new GameContext(), true), Square.E4);
        return ResponseEntity.noContent().build();
    }
}
