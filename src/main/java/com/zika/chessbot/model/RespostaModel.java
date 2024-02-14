package com.zika.chessbot.model;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class RespostaModel {
    private String mensagem;
    private Boolean sucesso;

    public RespostaModel() {}

    public RespostaModel(String mensagem, Boolean sucesso) {
        this.mensagem = mensagem;
        this.sucesso = sucesso;
    }
}
