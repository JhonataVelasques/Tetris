package com.tetris.model.tetromino;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Fábrica para criar instâncias de Tetrominós.
 * Utiliza o Padrão de Design Factory para encapsular a lógica de criação de peças,
 * permitindo gerar peças aleatórias de forma fácil e centralizada.
 */
public class TetrominoFactory {
    // Array de construtores de peças para facilitar a seleção aleatória
    private static final Supplier<Tetromino>[] PIECE_SUPPLIERS = new Supplier[]{
            IPiece::new,
            JPiece::new,
            LPiece::new,
            OPiece::new,
            SPiece::new,
            TPiece::new,
            ZPiece::new
    };

    private static final Random random = new Random();

    /**
     * @return Uma instância de Tetrominó selecionada aleatoriamente.
     */
    public static Tetromino getRandomTetromino() {
        return PIECE_SUPPLIERS[random.nextInt(PIECE_SUPPLIERS.length)].get();
    }
}