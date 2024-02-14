package com.zika.chessbot.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.zika.chessbot.model.TipoPartidaModel;

@Repository
public interface TipoPartidaRepository extends CrudRepository<TipoPartidaModel, UUID>{
    
}
