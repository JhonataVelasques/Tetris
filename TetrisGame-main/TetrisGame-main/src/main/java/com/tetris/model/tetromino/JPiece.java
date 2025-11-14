package com.tetris.model.tetromino;

import javafx.scene.paint.Color;

public class JPiece extends Tetromino {
    public JPiece() {
        super(new int[][]{
                {0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 1, 1, 0, 0},
                {0, 0, 0, 0, 0}
             /**
                {0, 1, 0},
                {0, 1, 0},
                {1, 1, 0}
              */
        }, Color.DEEPPINK);
    }
}

