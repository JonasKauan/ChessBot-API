package com.zika.chessbot.model;

import java.util.UUID;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "historico")
public class HistoricoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idHistorico;

    private UUID fkVencedor;

    private String corJogador;

    private Double precicao;

    @ManyToOne
    @JoinColumn(name = "fk_jogador")
    private JogadorModel jogador;

    @ManyToOne
    @JoinColumn(name = "fk_partida")
    private PartidaModel partida;
}
