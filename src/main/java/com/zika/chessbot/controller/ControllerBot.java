package com.zika.chessbot.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zika.chessbot.service.BotService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/bot")
public class ControllerBot {

    @GetMapping(value = "/movimento/")
    public Map<String, String> calcularMovimento(@RequestParam String fen) {
        return BotService.calcularMovimento(fen);
    }

    @GetMapping(value = "/precicao/")
    public Map<String, Double> calcularPrecisao(
        @RequestParam List<String> movimentos,
        @RequestParam int tamanhoAbertura
    ){
        return BotService.calcularPrecisao(movimentos, tamanhoAbertura);
    }
}
