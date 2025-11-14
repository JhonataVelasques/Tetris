package com.tetris.model;

import java.time.LocalDateTime;

public class Partida {
    private int id;
    private Jogador jogador;
    private int score;
    private LocalDateTime data;

    public Partida(int id, Jogador jogador, int score, LocalDateTime data) {
        this.id = id;
        this.jogador = jogador;
        this.score = score;
        this.data = data;
    }

    public int getId() { return id; }
    public Jogador getJogador() { return jogador; }
    public int getScore() { return score; }
    public LocalDateTime getData() { return data; }

    public void setId(int id) { this.id = id; }
    public void setJogador(Jogador jogador) { this.jogador = jogador; }
    public void setScore(int score) { this.score = score; }
    public void setData(LocalDateTime data) { this.data = data; }
}
