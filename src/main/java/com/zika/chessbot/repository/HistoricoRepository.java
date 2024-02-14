package com.zika.chessbot.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zika.chessbot.model.HistoricoModel;

@Repository
public interface HistoricoRepository extends CrudRepository<HistoricoModel, UUID> {

    @Query("SELECT h.partida.idPartida FROM HistoricoModel h WHERE h.jogador.idJogador = :jogadorId")
    Iterable<UUID> findIdPartidaByIdJogador(@Param("jogadorId") UUID jogadorId);

    @Query("SELECT h FROM HistoricoModel h WHERE h.partida.idPartida = :partidaId")
    Iterable<HistoricoModel> findByIdPartida(@Param("partidaId") UUID partidaId);
}
