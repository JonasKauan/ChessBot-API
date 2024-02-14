package com.zika.chessbot.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.zika.chessbot.bot.Search;

@Service
public class BotService {

    
    
    public static Map<String, String> calcularMovimento(String fen){
        return new HashMap<>(Map.of("response", new Search(fen, 3).decide()));
    }

    public static Map<String, Double> calcularPrecisao(List<String> movimentos, int tamanhoAbertura){
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

        double precisaoBrancas = 0.;
        double precisaoPretas = 0.;

        for(int i = 0; i < movimentos.size(); i++){
            Search s = new Search(fen, 3);
            if(i + 1 >= tamanhoAbertura){
                String movimentoBot = s.decide();
                if(movimentoBot.equals(movimentos.get(i))){
                    if(i % 2 == 0) precisaoBrancas++;
                    else precisaoPretas++;
                }
            }
            
            s.getBoard().doMove(movimentos.get(i));
            fen = s.getBoard().getFen();
        }

        precisaoBrancas /= movimentos.size();
        precisaoPretas /= movimentos.size();

        return new HashMap<>(Map.of(
            "brancas", precisaoBrancas,
            "pretas", precisaoPretas
        ));
    }
}
