package com.tetris.view;

import java.io.IOException;
import java.io.InputStream;

import com.tetris.controller.GameEngine;
import com.tetris.model.tetromino.Tetromino;

import javafx.animation.ScaleTransition;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import java.util.function.Consumer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
 

/**
 * Painel dedicado à exibição de informações como pontuação, nível,
 * linhas eliminadas e a pré-visualização da próxima peça.
 * Também contém os botões de controlo do jogo.
 */
public class InfoPanel extends VBox {

    private final Canvas nextPieceCanvas;
    private final GamePanel gamePanel; //Variável adicionada para troca de informações entre InfoPanel e GamePanel
    private final GameEngine gameEngine; // manter referência para atualizar preview ao trocar tema
    private Label scoreValueLabel; // referência para animar quando score aumentar
    private Button pauseButton;
    private Button restartButton;
    // styles
    private final String neon = "#39ff14"; // verde fluorescente
    private final String baseBg = "rgba(57,255,20,0.10)"; // fundo sutil
    private final String hoverBg = "rgba(57,255,20,0.22)"; // fundo destacado ao passar o mouse
    private String baseStyleDark;
    private String hoverStyleDark;
    private String baseStyleLight;
    private String hoverStyleLight;

    public InfoPanel(GameEngine gameEngine, GamePanel gamePanel) {
        this.gamePanel = gamePanel; // Inicialização da variável adicionada
        this.gameEngine = gameEngine;
        setSpacing(10);
        setPadding(new Insets(10));
        setAlignment(Pos.TOP_CENTER);
        setPrefWidth(200);

        // Fonte digital/monoespaçada (fallback para Consolas)
        Font titleFont = new Font("Consolas", 12);
        Font valueFont = new Font("Consolas", 28);

        // Tenta carregar fonte digital embutida em resources/fonts/digital.ttf
        try (InputStream is = getClass().getResourceAsStream("/fonts/digital.ttf")) {
            if (is != null) {
                Font loaded = Font.loadFont(is, 10); // carrega família da fonte
                if (loaded != null) {
                    titleFont = Font.font(loaded.getFamily(), 12);
                    valueFont = Font.font(loaded.getFamily(), 28);
                }
            }
        } catch (IOException e) {
            // não-fatal: mantém fallback
        }

        // Cria blocos digitais para score/level/lines
        VBox scoreBlock = createInfoBlock("SCORE", gameEngine.scoreProperty(), titleFont, valueFont);
        VBox levelBlock = createInfoBlock("LEVEL", gameEngine.levelProperty(), titleFont, valueFont);
        VBox linesBlock = createInfoBlock("LINES", gameEngine.linesClearedProperty(), titleFont, valueFont);

        Label nextPieceLabel = new Label("Next Piece:");
        nextPieceLabel.setFont(titleFont);

        nextPieceCanvas = new Canvas(4 * GamePanel.BLOCK_SIZE, 4 * GamePanel.BLOCK_SIZE);

        // Observa a propriedade da próxima peça para redesenhar o canvas
        gameEngine.nextPieceProperty().addListener((observable) -> drawNextPiece(gameEngine.nextPieceProperty().get()));

        this.pauseButton = new Button("Pause/Resume");
        this.pauseButton.setOnAction(evt -> {
            gameEngine.togglePause();
            gamePanel.requestFocus(); // restaura o foco para o gamePanel após clicar no botão pause/resume
            com.tetris.sound.SoundManager.getInstance().playUIButton();
        });

        this.restartButton = new Button("Restart");
        this.restartButton.setOnAction(evt -> {
            gameEngine.start();
            gamePanel.requestFocus(); // restaura o foco para o gamePanel após clicar no botão Restart.
            com.tetris.sound.SoundManager.getInstance().playUIButton();
        });

        // Controle de volume / mute
        Slider volumeSlider = new Slider(0, 1, com.tetris.sound.SoundManager.getInstance().getVolume());
        volumeSlider.setPrefWidth(120);
        volumeSlider.setBlockIncrement(0.05);
        volumeSlider.valueProperty().addListener((obs, oldV, newV) -> {
            com.tetris.sound.SoundManager.getInstance().setVolume(newV.doubleValue());
        });
        // Não roubar foco do game (para que as teclas continuem a funcionar enquanto ajusta volume)
        volumeSlider.setFocusTraversable(false);
        // Se o usuário clicar e soltar, garanta que o foco volte ao painel do jogo (fallback)
        volumeSlider.setOnMouseReleased(e -> gamePanel.requestFocus());

        Button muteButton = new Button();
        muteButton.setPrefWidth(48);
        muteButton.setText(com.tetris.sound.SoundManager.getInstance().isMuted() ? "🔇" : "🔊");
        muteButton.setOnAction(e -> {
            com.tetris.sound.SoundManager sm = com.tetris.sound.SoundManager.getInstance();
            sm.toggleMute();
            muteButton.setText(sm.isMuted() ? "🔇" : "🔊");
        });
        // Não permitir que o botão de mute roube foco (clicar não tira foco do game)
        muteButton.setFocusTraversable(false);
        muteButton.setOnMouseReleased(e -> gamePanel.requestFocus());

        HBox audioControls = new HBox(8, muteButton, volumeSlider);
        audioControls.setAlignment(Pos.CENTER);

        // Agrupa os blocos digitais (coluna)
        VBox statsCol = new VBox(8, scoreBlock, levelBlock, linesBlock);
        statsCol.setAlignment(Pos.TOP_CENTER);

    // Estiliza botões com cor verde fluorescente e fundo de destaque (dark/light variants)
    baseStyleDark = "-fx-text-fill: " + neon + "; -fx-font-family: Consolas; -fx-font-weight: bold;"
        + " -fx-background-color: " + baseBg + "; -fx-border-color: " + neon + "; -fx-border-width: 2;"
        + " -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 10 6 10;";
    hoverStyleDark = "-fx-text-fill: " + neon + "; -fx-font-family: Consolas; -fx-font-weight: bold;"
        + " -fx-background-color: " + hoverBg + "; -fx-border-color: " + neon + "; -fx-border-width: 2;"
        + " -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 10 6 10;";

    // Light variant: use dark background so neon stands out similar to dark theme
    baseStyleLight = "-fx-text-fill: " + neon + "; -fx-font-family: Consolas; -fx-font-weight: bold;"
        + " -fx-background-color: rgba(0,0,0,0.85); -fx-border-color: " + neon + "; -fx-border-width: 2;"
        + " -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 10 6 10;";
    hoverStyleLight = "-fx-text-fill: " + neon + "; -fx-font-family: Consolas; -fx-font-weight: bold;"
        + " -fx-background-color: rgba(57,255,20,0.35); -fx-border-color: " + neon + "; -fx-border-width: 2;"
        + " -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 10 6 10;";

    // Apply initial styles based on current theme
    boolean dark = Theme.isDark();
    pauseButton.setStyle(dark ? baseStyleDark : baseStyleLight);
    restartButton.setStyle(dark ? baseStyleDark : baseStyleLight);
    // Hover effects that respect current theme
    pauseButton.setOnMouseEntered(e -> { e.consume(); pauseButton.setStyle(Theme.isDark() ? hoverStyleDark : hoverStyleLight); });
    pauseButton.setOnMouseExited(e -> { e.consume(); pauseButton.setStyle(Theme.isDark() ? baseStyleDark : baseStyleLight); });
    restartButton.setOnMouseEntered(e -> { e.consume(); restartButton.setStyle(Theme.isDark() ? hoverStyleDark : hoverStyleLight); });
    restartButton.setOnMouseExited(e -> { e.consume(); restartButton.setStyle(Theme.isDark() ? baseStyleDark : baseStyleLight); });

    getChildren().addAll(statsCol, nextPieceLabel, nextPieceCanvas, pauseButton, restartButton, audioControls);

            // Aplica tema inicial e ouve mudanças do tema
            applyTheme(Theme.isDark());
            // Usa ChangeListener para aplicar o novo valor (evita parâmetros não usados)
            Theme.darkProperty().addListener((obs, oldV, newV) -> {
                // Referência rápida aos parâmetros para satisfazer verificadores estáticos (sem efeitos colaterais)
                if (obs == null && oldV == null) return;
                applyTheme(newV);
            });

        // Animação: quando o score aumentar, faz um pulso no valor
        gameEngine.scoreProperty().addListener((obs, oldV, newV) -> {
            if (oldV != null && newV != null && newV.intValue() > oldV.intValue()) {
                animateScoreIncrease();
                com.tetris.sound.SoundManager.getInstance().playScoreIncrease();
            }
        });
    }

    private void applyTheme(boolean dark) {
        if (dark) {
            setStyle("-fx-background-color: #222; -fx-text-fill: white;");
        } else {
            setStyle("-fx-background-color: #efefef; -fx-text-fill: black;");
        }

    // Atualiza o canvas da próxima peça para combinar com o tema e redesenha a peça atual
    GraphicsContext gc = nextPieceCanvas.getGraphicsContext2D();
    gc.setFill(dark ? Color.web("#ddd") : Color.LIGHTGRAY);
    gc.fillRect(0, 0, nextPieceCanvas.getWidth(), nextPieceCanvas.getHeight());

        // Atualiza cores dos labels filhos para garantir contraste e aplica contorno preto
        getChildren().forEach(node -> {
            if (node instanceof Label) {
                Label lbl = (Label) node;
                String style = lbl.getStyle();
                boolean isNeon = style != null && style.contains("#39ff14");
                if (isNeon) {
                    // mantém texto neon, mas no tema claro aplica contorno preto para destaque
                    lbl.setTextFill(Color.web("#39ff14"));
                    if (!dark) {
                        DropShadow outline = new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 3, 0.9, 0, 0);
                        outline.setSpread(0.8);
                        lbl.setEffect(outline);
                    } else {
                        lbl.setEffect(null);
                    }
                } else {
                    lbl.setTextFill(dark ? Color.WHITE : Color.BLACK);
                    lbl.setEffect(null);
                }
            }
        });

        // Atualiza estilos dos botões para manter o mesmo padrão visual em modo claro/escuro
        if (pauseButton != null && restartButton != null) {
            pauseButton.setStyle(dark ? baseStyleDark : baseStyleLight);
            restartButton.setStyle(dark ? baseStyleDark : baseStyleLight);
        }

    // Forçar o fundo dos blocos de informação (SCORE/LEVEL/LINES) a permanecer com
    // a mesma cor escura independente do tema claro/escuro, para maior contraste
    // Opacidade ajustada para 0.35 (melhor contraste no modo claro)
    String infoBlockBg = "-fx-padding: 6 10 6 10; -fx-background-radius: 6; -fx-background-color: rgba(0,0,0,0.35);";
        // percorre recursivamente todos os nós filhos para encontrar os blocos de info
        Consumer<javafx.scene.Node> applyToNode = new Consumer<javafx.scene.Node>() {
            @Override
            public void accept(javafx.scene.Node node) {
                if (node instanceof VBox) {
                    String id = node.getId();
                    if (id != null && id.startsWith("info-block-")) {
                        node.setStyle(infoBlockBg);
                    }
                }
                if (node instanceof Parent) {
                    for (javafx.scene.Node child : ((Parent) node).getChildrenUnmodifiable()) {
                        accept(child);
                    }
                }
            }
        };
        applyToNode.accept(this);

        // Redesenha a próxima peça para garantir que a prévia não desapareça ao alternar tema
        try {
            drawNextPiece(gameEngine.nextPieceProperty().get());
        } catch (Exception ex) {
            // silenciar: se gameEngine ainda não tiver próxima peça, não falha
        }
    }

    // método createInfoLabel removido: InfoPanel agora usa blocos digitais (createInfoBlock)

    private VBox createInfoBlock(String title, IntegerProperty valueProperty, Font titleFont, Font valueFont) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(titleFont);
        titleLabel.setStyle("-fx-text-fill: #39ff14; -fx-opacity: 0.95; -fx-letter-spacing: 1px;");

        Label valueLabel = new Label("0");
        valueLabel.setFont(valueFont);
        valueLabel.setStyle("-fx-text-fill: #39ff14; -fx-font-weight: bold;");
    valueLabel.textProperty().bind(valueProperty.asString("%d"));

        // se for o bloco de SCORE, guarda referência para animação
        if ("SCORE".equals(title)) {
            this.scoreValueLabel = valueLabel;
        }

        VBox box = new VBox(2, titleLabel, valueLabel);
        // identificar blocos de informação para permitir ajustes de tema específicos
        box.setId("info-block-" + title.toLowerCase());
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding: 6 10 6 10; -fx-background-radius: 6; -fx-background-color: rgba(0,0,0,0.35);");
        return box;
    }

    private void drawNextPiece(Tetromino piece) {
        GraphicsContext gc = nextPieceCanvas.getGraphicsContext2D();
        gc.setFill(Theme.isDark() ? Color.BLACK : Color.web("#f8f8f8"));
        gc.fillRect(0, 0, nextPieceCanvas.getWidth(), nextPieceCanvas.getHeight());

        if (piece != null) {
            int[][] shape = piece.getShape();
            Color color = piece.getColor();
            double blockSize = GamePanel.BLOCK_SIZE * 0.8; // Um pouco menor para caber bem
            double startX = (nextPieceCanvas.getWidth() - (shape[0].length * blockSize)) / 2;
            double startY = (nextPieceCanvas.getHeight() - (shape.length * blockSize)) / 2;

            for (int y = 0; y < shape.length; y++) {
                for (int x = 0; x < shape[y].length; x++) {
                    if (shape[y][x] != 0) {
                        gc.setFill(color);
                        gc.fillRect(startX + x * blockSize, startY + y * blockSize, blockSize, blockSize);
                        gc.setStroke(Color.BLACK);
                        gc.strokeRect(startX + x * blockSize, startY + y * blockSize, blockSize, blockSize);
                    }
                }
            }
        }
    }

    // Animação pública para disparar externamente, se necessário
    private void animateScoreIncrease() {
        if (scoreValueLabel == null) return;
        ScaleTransition st = new ScaleTransition(Duration.millis(220), scoreValueLabel);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.25);
        st.setToY(1.25);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.playFromStart();
    }

    // Observador para animar quando o score aumentar
    {
        // bloco de inicialização para adicionar listener depois que os campos forem inicializados
    }
}