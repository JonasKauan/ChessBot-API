package com.zika.chessbot.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zika.chessbot.model.JogadorModel;
import com.zika.chessbot.service.JogadorService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/jogador")
@CrossOrigin(origins = "*")
public class ControllerJogador {

    @Autowired
    private JogadorService jogadorService;

    @GetMapping("/")
    public Iterable<JogadorModel> listarTodosJogadores() {
        return jogadorService.listar();
    }

    @GetMapping("/login/{email}/{senha}")
    public JogadorModel logar(@PathVariable String email, @PathVariable String senha) {
        return jogadorService.listarPorEmailSenha(email, senha);
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarJogador(@RequestBody JogadorModel jogador) {
        return jogadorService.cadastrarJogador(jogador);
    }

    @PutMapping("/alterar")
    public ResponseEntity<?> alterarJogador(@RequestBody JogadorModel jogador) {
        return jogadorService.alterar(jogador);
    }

    @DeleteMapping("/remover/{id}")
    public ResponseEntity<?> deletarJogador(@PathVariable UUID id) {
        return jogadorService.remover(id);
    }
}