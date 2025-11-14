package com.tetris.view;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import com.tetris.controller.GameEngine;
import com.tetris.model.Board;
import com.tetris.model.GameState;
import com.tetris.model.tetromino.Tetromino;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Componente da Interface Gráfica responsável por renderizar o estado visual do jogo.
 * Utiliza um Canvas JavaFX para desenhar o tabuleiro, as peças fixas e a peça em movimento.
 */
public class GamePanel extends Canvas {

    public static final int BLOCK_SIZE = 30;
    private final GameEngine gameEngine;
    private final GraphicsContext gc;

    public GamePanel(GameEngine gameEngine) {
        super(Board.WIDTH * BLOCK_SIZE, Board.HEIGHT * BLOCK_SIZE);
        this.gameEngine = gameEngine;
        this.gc = getGraphicsContext2D();

        // Um loop de renderização para manter 60 FPS
        AnimationTimer renderer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw();
            }
        };
        renderer.start();
    }

    private void draw() {
        // Limpa o canvas
        gc.setFill(Theme.isDark() ? Color.BLACK : Color.web("#f8f8f8"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        // Desenha grade auxiliar para facilitar posicionamento
        drawGrid();

        // Desenha o tabuleiro (peças já fixadas)
        Board board = gameEngine.getBoard();
        Color[][] grid = board.getGrid();
        for (int y = 0; y < Board.HEIGHT; y++) {
            for (int x = 0; x < Board.WIDTH; x++) {
                if (grid[y][x] != null) {
                    drawBlock(x, y, grid[y][x]);
                }
            }
        }

        // Desenha a peça atual
        Tetromino currentPiece = gameEngine.getCurrentPiece();
        // Desenha ghost (onde a peça irá cair)
        drawGhost(currentPiece);
        if (currentPiece != null) {
            drawTetromino(currentPiece);
        }

        // Desenha sobreposições de estado (Pausado, Game Over)
        GameState state = gameEngine.gameStateProperty().get();
        if (state == GameState.PAUSED) {
            drawOverlay("PAUSED");
        } else if (state == GameState.GAME_OVER) {
            drawOverlay("GAME OVER");
        }
    }

    private void drawGrid() {
        gc.setLineWidth(1);
        Color gridColor = Theme.isDark() ? Color.web("#333333", 0.6) : Color.web("#cccccc", 0.6);
        gc.setStroke(gridColor);
        // vertical lines
        for (int x = 0; x <= Board.WIDTH; x++) {
            double px = x * BLOCK_SIZE + 0.5; // 0.5 for crisper lines
            gc.strokeLine(px, 0, px, getHeight());
        }
        // horizontal lines
        for (int y = 0; y <= Board.HEIGHT; y++) {
            double py = y * BLOCK_SIZE + 0.5;
            gc.strokeLine(0, py, getWidth(), py);
        }
    }

    private void drawGhost(Tetromino piece) {
        if (piece == null) return;
        Board board = gameEngine.getBoard();
        try {
            // create new instance of the same concrete class
            Constructor<? extends Tetromino> ctor = (Constructor<? extends Tetromino>) piece.getClass().getDeclaredConstructor();
            ctor.setAccessible(true);
            Tetromino ghost = ctor.newInstance();
            // match orientation by rotating up to 4 times
            int[][] target = piece.getShape();
            for (int r = 0; r < 4; r++) {
                if (Arrays.deepEquals(ghost.getShape(), target)) break;
                ghost.rotate();
            }
            ghost.setX(piece.getX());
            ghost.setY(piece.getY());

            // drop until invalid
            while (board.isValidPosition(ghost)) {
                ghost.setY(ghost.getY() + 1);
            }
            ghost.setY(ghost.getY() - 1);

            // draw translucent blocks for ghost
            int[][] shape = ghost.getShape();
            Color ghostColor = Color.web("#39ff14", 0.22);
            for (int y = 0; y < shape.length; y++) {
                for (int x = 0; x < shape[y].length; x++) {
                    if (shape[y][x] != 0) {
                        int bx = ghost.getX() + x;
                        int by = ghost.getY() + y;
                        if (by >= 0 && bx >= 0 && bx < Board.WIDTH && by < Board.HEIGHT) {
                            gc.setFill(ghostColor);
                            gc.fillRect(bx * BLOCK_SIZE, by * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                            gc.setStroke(Color.web("#000000", 0.25));
                            gc.strokeRect(bx * BLOCK_SIZE, by * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                        }
                    }
                }
            }

        } catch (Exception e) {
            // fallback: não desenhar ghost em caso de erro
        }
    }

    private void drawTetromino(Tetromino piece) {
        int[][] shape = piece.getShape();
        Color color = piece.getColor();
        for (int y = 0; y < shape.length; y++) {
            for (int x = 0; x < shape[y].length; x++) {
                if (shape[y][x] != 0) {
                    drawBlock(piece.getX() + x, piece.getY() + y, color);
                }
            }
        }
    }

    private void drawBlock(int x, int y, Color color) {
        gc.setFill(color);
        gc.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
        gc.setStroke(Theme.isDark() ? Color.WHITE : Color.BLACK);
        gc.strokeRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
    }

    private void drawOverlay(String text) {
        // overlay semitransparente respeitando o tema
        if (Theme.isDark()) {
            gc.setFill(new Color(0, 0, 0, 0.7));
            gc.fillRect(0, 0, getWidth(), getHeight());
            gc.setFill(Color.WHITE);
        } else {
            gc.setFill(new Color(1, 1, 1, 0.8));
            gc.fillRect(0, 0, getWidth(), getHeight());
            gc.setFill(Color.BLACK);
        }
        gc.setFont(new Font("Arial", 40));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, getWidth() / 2, getHeight() / 2);
    }
}