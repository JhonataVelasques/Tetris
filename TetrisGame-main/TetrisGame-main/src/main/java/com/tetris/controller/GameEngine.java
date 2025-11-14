package com.tetris.controller;

import com.tetris.model.Board;
import com.tetris.model.GameState;
import com.tetris.model.tetromino.Tetromino;
import com.tetris.model.tetromino.TetrominoFactory;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyCode;

/**
 * Motor do Jogo (GameEngine).
 * Classe central que coordena toda a lógica, o estado e o loop principal do jogo.
 * Conecta o modelo (Board, Tetromino) com a visão (GamePanel, InfoPanel).
 */
public class GameEngine {

    private final Board board;
    private Tetromino currentPiece;
    private Tetromino nextPiece;

    // Propriedades JavaFX para notificar a UI sobre mudanças (Padrão Observer)
    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final IntegerProperty linesCleared = new SimpleIntegerProperty(0);
    private final ObjectProperty<GameState> gameState = new SimpleObjectProperty<>(GameState.PLAYING);
    private final ObjectProperty<Tetromino> nextPieceProperty = new SimpleObjectProperty<>();

    private final AnimationTimer gameLoop;
    private long lastDropTime;

    private String nomeJogador = "Jogador";

    public GameEngine() {
        this.board = new Board();
        this.gameLoop = createGameLoop();
    }

    private AnimationTimer createGameLoop() {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameState.get() == GameState.PLAYING) {
                    if (now - lastDropTime >= getDropInterval()) {
                        moveDown();
                        lastDropTime = now;
                    }
                }
            }
        };
    }

    public void start() {
        board.clear();
        score.set(0);
        level.set(1);
        linesCleared.set(0);
        currentPiece = TetrominoFactory.getRandomTetromino();
        nextPiece = TetrominoFactory.getRandomTetromino();
        nextPieceProperty.set(nextPiece);
        gameState.set(GameState.PLAYING);
        lastDropTime = System.nanoTime();
        gameLoop.start();
    }


    public void handleKeyPress(KeyCode code) {
        if (gameState.get() != GameState.PLAYING) {
            return;
        }
        switch (code) {
            case LEFT:  moveLeft(); break;
            case RIGHT: moveRight(); break;
            case DOWN:  moveDown(); com.tetris.sound.SoundManager.getInstance().playSoftDrop(); break;
            case UP:    rotate(); break;
            case SPACE: hardDrop(); break;
        }
    }

    public void togglePause() {
        if (gameState.get() == GameState.PLAYING) {
            gameState.set(GameState.PAUSED);
            gameLoop.stop();
            com.tetris.sound.SoundManager.getInstance().playPause();
        } else if (gameState.get() == GameState.PAUSED) {
            gameState.set(GameState.PLAYING);
            lastDropTime = System.nanoTime();
            gameLoop.start();
            com.tetris.sound.SoundManager.getInstance().playPause();
        }
    }

    private void move(int dx, int dy) {
        currentPiece.setX(currentPiece.getX() + dx);
        currentPiece.setY(currentPiece.getY() + dy);
        if (!board.isValidPosition(currentPiece)) {
            // Reverte o movimento se for inválido
            currentPiece.setX(currentPiece.getX() - dx);
            currentPiece.setY(currentPiece.getY() - dy);
            // Se o movimento inválido foi para baixo, a peça travou
            if (dy > 0) {
                lockPiece();
            }
        }
    }

    private void moveLeft() { move(-1, 0); }
    private void moveRight() { move(1, 0); }
    private void moveDown() { move(0, 1); }

    private void hardDrop() {
        while (board.isValidPosition(currentPiece)) {
            currentPiece.setY(currentPiece.getY() + 1);
        }
        currentPiece.setY(currentPiece.getY() - 1); // Volta uma posição
        // reproduce som de hard drop e tranca a peça
        com.tetris.sound.SoundManager.getInstance().playHardDrop();
        lockPiece();
    }

    private void rotate() {
        currentPiece.rotate();
        // som de rotação
        com.tetris.sound.SoundManager.getInstance().playRotate();
        if (!board.isValidPosition(currentPiece)) {
            // Tenta "chutar" a peça para uma posição válida (wall kick simplificado)
            move(1, 0); if (board.isValidPosition(currentPiece)) return; move(-2, 0);
            if (board.isValidPosition(currentPiece)) return; move(1, 0);

            // Se não for possível, reverte a rotação
            currentPiece.rotate(); currentPiece.rotate(); currentPiece.rotate();
        }
    }

    private void lockPiece() {
        board.placePiece(currentPiece);
        // som de encaixe
        com.tetris.sound.SoundManager.getInstance().playLock();
        int cleared = board.clearLines();
        if (cleared > 0) {
            updateScore(cleared);
            // som de clear baseado em quantas linhas
            com.tetris.sound.SoundManager.getInstance().playClear(cleared);
        }
        spawnNewPiece();
    }

    private void spawnNewPiece() {
        currentPiece = nextPiece;
        nextPiece = TetrominoFactory.getRandomTetromino();
        nextPieceProperty.set(nextPiece);

        if (!board.isValidPosition(currentPiece)) {
            gameState.set(GameState.GAME_OVER);
            gameLoop.stop();
            com.tetris.sound.SoundManager.getInstance().playGameOver();

            // Registro automático da partida no banco
            try {
                int scoreFinal = score.get();
                com.tetris.model.dao.SqlServerPlayerDAO playerDAO = new com.tetris.model.dao.SqlServerPlayerDAO();
                playerDAO.addPlayer(new com.tetris.model.Player(nomeJogador, scoreFinal));
            } catch (Exception ex) {
                System.err.println("Falha ao registrar partida: " + ex.getMessage());
            }
        }
    }

    private void updateScore(int cleared) {
        linesCleared.set(linesCleared.get() + cleared);
        int points;
        switch (cleared) {
            case 1: points = 40; break;
            case 2: points = 100; break;
            case 3: points = 300; break;
            case 4: points = 1200; break;
            default: points = 0;
        }
        score.set(score.get() + points * level.get());
        updateLevel();
    }

    private void updateLevel() {
        level.set((linesCleared.get() / 10) + 1);
    }

    private long getDropInterval() {
        int currentLevel = level.get();
        double seconds;
        if (currentLevel <= 3) {
            seconds = 1.0;
        } else if (currentLevel <= 6) {
            seconds = 0.7;
        } else {
            seconds = 0.4;
        }
        return (long) (seconds * 1_000_000_000L); // em nanossegundos
    }

    // Getters para as propriedades, para a UI poder observá-las
    public Board getBoard() { return board; }
    public Tetromino getCurrentPiece() { return currentPiece; }
    public IntegerProperty scoreProperty() { return score; }
    public IntegerProperty levelProperty() { return level; }
    public IntegerProperty linesClearedProperty() { return linesCleared; }
    public ObjectProperty<GameState> gameStateProperty() { return gameState; }
    public ObjectProperty<Tetromino> nextPieceProperty() { return nextPieceProperty; }

    public void setNomeJogador(String nome) {
        this.nomeJogador = nome;
    }
}