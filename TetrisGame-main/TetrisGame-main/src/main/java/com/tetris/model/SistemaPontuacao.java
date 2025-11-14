package com.tetris.model;

public class SistemaPontuacao {
    public static int calcularPontuacao(int linhasRemovidas, int nivel) {
        switch (linhasRemovidas) {
            case 1: return 40 * (nivel + 1);
            case 2: return 100 * (nivel + 1);
            case 3: return 300 * (nivel + 1);
            case 4: return 1200 * (nivel + 1);
            default: return 0;
        }
    }
}
