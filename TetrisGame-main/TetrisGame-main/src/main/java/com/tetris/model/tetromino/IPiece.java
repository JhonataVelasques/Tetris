package com.tetris.model.tetromino;

import javafx.scene.paint.Color;

public class IPiece extends Tetromino {
    public IPiece() {
        super(new int[][]{
                {0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0}
        }, Color.BLUE);
    }
}

