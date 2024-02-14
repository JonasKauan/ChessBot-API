package com.zika.chessbot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zika.chessbot.model.PartidaModel;
import com.zika.chessbot.service.PartidaService;

@RestController
@RequestMapping("/partida")
@CrossOrigin(origins = "*")

public class ControllerPartida {
    
    @Autowired
    private PartidaService partidaService;
    
    @GetMapping("/")
    public Iterable<PartidaModel> listarPartidas(){
        return partidaService.listar();
    }

    @PostMapping("/cadastrar")
    public PartidaModel cadastrarPartida(@RequestBody PartidaModel partida){
        return partidaService.cadastrar(partida);
    }

    @PutMapping("/alterar")
    public ResponseEntity<?> alterarPartida(@RequestBody PartidaModel partida){
        return partidaService.alterar(partida);
    }
}
