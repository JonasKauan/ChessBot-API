package com.zika.chessbot.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.zika.chessbot.model.JogadorModel;
import com.zika.chessbot.model.RespostaModel;
import com.zika.chessbot.repository.JogadorRepository;

@Service
public class JogadorService {

    @Autowired
    private JogadorRepository jogadorRepository;

    public Iterable<JogadorModel> listar() {
        return jogadorRepository.findAll();
    }

    public JogadorModel listarPorEmailSenha(String email, String senha) {
        return jogadorRepository.findByEmailAndSenha(email, senha);
    }

    public ResponseEntity<?> cadastrarJogador(JogadorModel jogador) {
        RespostaModel resposta = new RespostaModel();

        boolean emailJaCadastrado = jogadorRepository.findByEmail(jogador.getEmail()) != null;

        if (emailJaCadastrado)
            resposta.setMensagem("E-mail já cadastrado");

        boolean nomeJaExiste = jogadorRepository.findByNomeJogador(jogador.getNomeJogador()) != null;

        if (nomeJaExiste) {
            StringBuilder sb = new StringBuilder();

            if (resposta.getMensagem() != null) sb.append(resposta.getMensagem()).append("\n");
        
            sb.append("Nome de usuário em uso");
            resposta.setMensagem(sb.toString());
        }

        resposta.setSucesso(!emailJaCadastrado && !nomeJaExiste);

        if (!resposta.getSucesso())
            return new ResponseEntity<>(resposta, HttpStatus.CONFLICT);

        jogadorRepository.save(jogador);
        resposta.setMensagem("Cadastro realizado com sucesso");
        resposta.setSucesso(true);
        return new ResponseEntity<>(resposta, HttpStatus.CREATED);
    }

    public ResponseEntity<?> alterar(JogadorModel jogador) {
        jogadorRepository.save(jogador);
        return new ResponseEntity<>(
            new RespostaModel("Jogador Atualizado", true),
            HttpStatus.OK
        );
    }

    public ResponseEntity<?> remover(UUID id) {
        jogadorRepository.deleteById(id);
        return new ResponseEntity<>(
            new RespostaModel("Jogador removido com sucesso", true),
            HttpStatus.OK
        );
    }
}
