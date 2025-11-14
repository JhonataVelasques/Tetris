package com.tetris.model.tetromino;

import javafx.scene.paint.Color;

public class LPiece extends Tetromino {
    public LPiece() {
        super(new int[][]{
                {0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0}
        }, Color.ORANGE);
    }
}
