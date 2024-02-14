package com.zika.chessbot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.zika.chessbot.model.AberturaModel;

@Repository
public interface AberturaRepository extends CrudRepository<AberturaModel, Long>{
    
}
