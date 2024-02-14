package com.zika.chessbot.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zika.chessbot.model.HistoricoModel;
import com.zika.chessbot.repository.HistoricoRepository;

@Service
public class HistoricoService {

    @Autowired
    HistoricoRepository historicoRepository;

    public Iterable<HistoricoModel> listar() {
        return historicoRepository.findAll();
    }

    public Iterable<UUID> listarPorFkJogador(UUID idJogador) {
        return historicoRepository.findIdPartidaByIdJogador(idJogador);
    }

    public Iterable<HistoricoModel> listarPorFkPartida(UUID IdPartida) {
        return historicoRepository.findByIdPartida(IdPartida);
    }

    public HistoricoModel cadastrar(HistoricoModel historicoModel) {
        return historicoRepository.save(historicoModel);
    }


}
