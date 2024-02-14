package com.zika.chessbot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zika.chessbot.model.AberturaModel;
import com.zika.chessbot.service.AberturaService;

@RestController
@RequestMapping("/abertura")
public class ControllerAbertura {
    
    @Autowired
    private AberturaService aberturaService;

    @GetMapping("/")
    public Iterable<AberturaModel> listarAberturas(){
        return aberturaService.listar();
    }
}
