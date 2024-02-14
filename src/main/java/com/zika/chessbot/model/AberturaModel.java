package com.zika.chessbot.model;


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
@Table(name = "abertura")
public class AberturaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAbertura;

    private String nomeAbertura;

    private String movimentosAbertura;
}
