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
@Table(name = "partida")
public class PartidaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idPartida;

    private String movimentosPartida;

    private Double tempoPercorrido;

    @ManyToOne
    @JoinColumn(name = "fk_tipo_partida")
    private TipoPartidaModel tipoPartida;

    @ManyToOne
    @JoinColumn(name = "fk_abertura")
    private AberturaModel abertura;
}
