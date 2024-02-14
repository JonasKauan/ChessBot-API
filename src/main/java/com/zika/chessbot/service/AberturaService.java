package com.zika.chessbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zika.chessbot.model.AberturaModel;
import com.zika.chessbot.repository.AberturaRepository;

@Service
public class AberturaService {

    @Autowired
    private AberturaRepository aberturaRepository;

    public Iterable<AberturaModel> listar() {
        return aberturaRepository.findAll();
    }
}
