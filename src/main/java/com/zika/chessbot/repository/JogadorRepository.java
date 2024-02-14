package com.zika.chessbot.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.zika.chessbot.model.JogadorModel;

@Repository
public interface JogadorRepository extends CrudRepository<JogadorModel, UUID> {
    JogadorModel findByEmailAndSenha(String email, String senha);
    JogadorModel findByEmail(String email);
    JogadorModel findByNomeJogador(String nomeJogador);
}
