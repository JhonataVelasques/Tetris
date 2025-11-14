package com.tetris.model.tetromino;

import javafx.scene.paint.Color;

public class SPiece extends Tetromino {
    public SPiece() {
        super(new int[][]{
                {0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0},
                {0, 1, 1, 0, 0},
                {0, 0, 0, 0, 0}
        }, Color.RED);
    }
}

