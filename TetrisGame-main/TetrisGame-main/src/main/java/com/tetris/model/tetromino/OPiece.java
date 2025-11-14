package com.tetris.model.tetromino;

import javafx.scene.paint.Color;

public class OPiece extends Tetromino {
    public OPiece() {
        super(new int[][]{
                {1, 1},
                {1, 1}
        }, Color.YELLOW);
    }

    /**
     * Sobrescreve o método de rotação.
     * Como um quadrado não muda ao girar, este método não faz nada.
     */
    @Override
    public void rotate(){
    }
}
