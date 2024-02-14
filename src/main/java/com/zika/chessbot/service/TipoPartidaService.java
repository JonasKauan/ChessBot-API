package com.zika.chessbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zika.chessbot.model.TipoPartidaModel;
import com.zika.chessbot.repository.TipoPartidaRepository;

@Service
public class TipoPartidaService {
    @Autowired
    private TipoPartidaRepository tipoRepository;

    public Iterable<TipoPartidaModel> listar(){
        return tipoRepository.findAll();
    }
}
