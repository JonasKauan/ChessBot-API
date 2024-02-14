package com.zika.chessbot.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.zika.chessbot.model.PartidaModel;

@Repository
public interface PartidaRepository extends CrudRepository<PartidaModel, UUID>{
    
}
