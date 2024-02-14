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
@Table(name = "jogador")
public class JogadorModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idJogador;
    private Integer rating;
    private String nomeJogador;
    private String email;
    private String senha;
}
