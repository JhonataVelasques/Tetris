package com.tetris.model.tetromino;

import javafx.scene.paint.Color;

public class TPiece extends Tetromino {
    public TPiece() {
        super(new int[][]{
                {0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 1, 1, 1, 0},
                {0, 0, 0, 0, 0}
        }, Color.PURPLE);
    }
}
