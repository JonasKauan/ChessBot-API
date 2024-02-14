package com.zika.chessbot.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tipo_partida")
public class TipoPartidaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idTipoPartida;
    
    private String nomeTipoPartida;
    
    private Double tempo;

    public TipoPartidaModel(String nomeTipoPartida, Double tempo){
        this.nomeTipoPartida = nomeTipoPartida;
        this.tempo = tempo;
    }

    public TipoPartidaModel(){}
}
