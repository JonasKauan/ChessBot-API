package com.zika.chessbot.controller;


import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zika.chessbot.model.HistoricoModel;
import com.zika.chessbot.service.HistoricoService;

@RestController
@RequestMapping("/historico")
@CrossOrigin(origins = "*")
public class ControllerHistorico {
    
    @Autowired
    HistoricoService historicoService;

    @GetMapping("/")
    public Iterable<HistoricoModel> listar(){
        return historicoService.listar();
    }

    @GetMapping("/jogador/{fkJogador}")
    public Iterable<UUID> listarPorFkJogador(@PathVariable UUID fkJogador){
        return historicoService.listarPorFkJogador(fkJogador);
    }

    @GetMapping("/partida/{fkPartida}")
    public Iterable<HistoricoModel> listarPorFkPartida(@PathVariable UUID fkPartida){
        return historicoService.listarPorFkPartida(fkPartida);
    }

    @PostMapping("/cadastrar")
    public HistoricoModel cadastrar(@RequestBody HistoricoModel historico){
        return historicoService.cadastrar(historico);
    }
}
