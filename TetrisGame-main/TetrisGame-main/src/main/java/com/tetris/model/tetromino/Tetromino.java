package com.tetris.model.tetromino;

import javafx.scene.paint.Color;

/**
 * Classe abstrata que representa um Tetrominó (uma peça do Tetris).
 * Define a estrutura e os comportamentos básicos que todas as peças devem ter,
 * como forma, cor, posição e rotação.
 */
public abstract class Tetromino {
    // Matriz que define a forma da peça. 1 representa um bloco, 0 é espaço vazio.
    protected int[][] shape;
    // Cor da peça.
    protected Color color;
    // Posição da peça no tabuleiro (coordenada do canto superior esquerdo da matriz shape).
    protected int x, y;

    public Tetromino(int[][] shape, Color color) {
        this.shape = shape;
        this.color = color;
        // Posição inicial padrão
        this.x = 3; // Inicia no centro do tabuleiro (10/2 - 2)
        this.y = 0; // Inicia no topo
    }

    /**
     * Rotaciona a peça no sentido horário.
     * A implementação padrão transpõe a matriz da forma e inverte suas linhas.
     * Este método pode ser sobrescrito por peças com comportamento de rotação especial (ex: OPiece).
     */
    public void rotate() {
        int size = shape.length;
        int[][] newShape = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                newShape[c][size - 1 - r] = shape[r][c];
            }
        }
        shape = newShape;
    }

    // Getters para acesso externo
    public int[][] getShape() { return shape; }
    public Color getColor() { return color; }
    public int getX() { return x; }
    public int getY() { return y; }

    // Setters para manipulação pela GameEngine
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}