package com.zika.chessbot;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.zika.chessbot.model.TipoPartidaModel;
import com.zika.chessbot.repository.TipoPartidaRepository;

@SpringBootApplication
public class ChessBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChessBotApplication.class, args);
	}

	@Bean
	public CommandLineRunner mappingDemo(TipoPartidaRepository tipoPartidaRepository) {
		boolean rodar = false;

		return args -> {
			if (rodar) {
				TipoPartidaModel tipo1 = new TipoPartidaModel("Bullet", 1.);
				TipoPartidaModel tipo2 = new TipoPartidaModel("Blitz 3min", 3.);
				TipoPartidaModel tipo3 = new TipoPartidaModel("Blitz 5min", 5.);
				TipoPartidaModel tipo4 = new TipoPartidaModel("Rápida 10min", 10.);
				TipoPartidaModel tipo5 = new TipoPartidaModel("Rápida 30min", 30.);
				TipoPartidaModel tipo6 = new TipoPartidaModel("Customizada", null);

				tipoPartidaRepository.saveAll(Arrays.asList(tipo1, tipo2, tipo3, tipo4, tipo5, tipo6));
			}
		};
	}
}

// ──────▄▀▄─────▄▀▄
// ─────▄█░░▀▀▀▀▀░░█▄
// ─▄▄──█░░░░░░░░░░░█──▄▄
// █▄▄█─█░░▀░░┬░░▀░░█─█▄▄█ 	ULYSSES
