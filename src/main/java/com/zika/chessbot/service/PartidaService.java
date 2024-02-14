package com.zika.chessbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.zika.chessbot.model.PartidaModel;
import com.zika.chessbot.model.RespostaModel;
import com.zika.chessbot.repository.PartidaRepository;

@Service
public class PartidaService {
    
    @Autowired
    private PartidaRepository partidaRepository;

    public Iterable<PartidaModel> listar(){
        return partidaRepository.findAll();
    }

    public PartidaModel cadastrar(PartidaModel partida){
        return partidaRepository.save(partida);
    }

    public ResponseEntity<?> alterar(PartidaModel partida){
        partidaRepository.save(partida);
        return new ResponseEntity<>(
            new RespostaModel("Partida alterada", true),
            HttpStatus.OK
        );
    }
}
