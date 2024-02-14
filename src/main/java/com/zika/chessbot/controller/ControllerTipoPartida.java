package com.zika.chessbot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zika.chessbot.model.TipoPartidaModel;
import com.zika.chessbot.service.TipoPartidaService;

@RestController
@RequestMapping("/tipo-partida")
@CrossOrigin(origins = "*")

public class ControllerTipoPartida {

    @Autowired
    TipoPartidaService tipoService;

    @GetMapping("/")
    public Iterable<TipoPartidaModel> listarTodosTipos(){
        return tipoService.listar();
    }
}
