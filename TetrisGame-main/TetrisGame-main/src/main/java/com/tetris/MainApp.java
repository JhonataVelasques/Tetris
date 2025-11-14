package com.tetris;

import java.io.InputStream;

import com.tetris.controller.GameEngine;
import com.tetris.model.Player;
import com.tetris.model.dao.SqlServerPlayerDAO;
import com.tetris.view.GamePanel;
import com.tetris.view.InfoPanel;
import com.tetris.view.Theme;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Classe principal que inicia a aplicação JavaFX.
 * Responsável por criar a janela, instanciar o GameEngine e
 * montar os componentes da interface gráfica (GamePanel e InfoPanel).
 */
public class MainApp extends Application {
    private String nomeSelecionado = "Jogador";

    @Override
    public void start(Stage primaryStage) {
        SqlServerPlayerDAO playerDAO = new SqlServerPlayerDAO();
        // Tela inicial antes do jogo
        javafx.scene.control.Dialog<String> startDialog = new javafx.scene.control.Dialog<>();
        startDialog.setTitle("Bem-vindo ao Tetris Jhow");
        startDialog.setHeaderText("Selecione uma opção para começar");
        ButtonType btnNovoJogador = new ButtonType("Novo Jogador", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        ButtonType btnContinuar = new ButtonType("Continuar Partida", javafx.scene.control.ButtonBar.ButtonData.YES);
        ButtonType btnVerRecorde = new ButtonType("Ver Último Recorde", javafx.scene.control.ButtonBar.ButtonData.HELP);
        startDialog.getDialogPane().getButtonTypes().addAll(btnNovoJogador, btnContinuar, btnVerRecorde);

        startDialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnNovoJogador) return "novo";
            if (dialogButton == btnContinuar) return "continuar";
            if (dialogButton == btnVerRecorde) return "recorde";
            return null;
        });

        String escolha = startDialog.showAndWait().orElse("");
        if ("novo".equals(escolha)) {
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
            dialog.setTitle("Adicionar Jogador");
            dialog.setHeaderText("Novo Jogador");
            dialog.setContentText("Nome:");
            dialog.showAndWait().ifPresent(nome -> {
                nomeSelecionado = nome;
                try {
                    playerDAO.addPlayer(new Player(nome, 0));
                    java.util.List<Player> jogadores = playerDAO.getAllPlayers();
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Jogadores");
                    alert.setHeaderText("Lista de Jogadores");
                    String lista = jogadores.stream().map(p -> p.getName() + " - " + p.getScore()).reduce("", (a, b) -> a + "\n" + b);
                    alert.setContentText(lista);
                    alert.showAndWait();
                } catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
            });
        } else if ("recorde".equals(escolha)) {
            try {
                java.util.List<Player> jogadores = playerDAO.getAllPlayers();
                Player recordista = jogadores.stream().max(java.util.Comparator.comparingInt(Player::getScore)).orElse(null);
                String recorde = (recordista == null) ? "Nenhum recorde registrado." : recordista.getName() + " - " + recordista.getScore();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Último Recorde");
                alert.setHeaderText("Recorde Atual");
                alert.setContentText(recorde);
                alert.showAndWait();
            } catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
        }

        BorderPane root = new BorderPane();

        // 1. Instanciar o motor do jogo
        GameEngine gameEngine = new GameEngine();
        gameEngine.setNomeJogador(nomeSelecionado);

        // 2. Criar e posicionar os painéis da UI
        GamePanel gamePanel = new GamePanel(gameEngine);
        InfoPanel infoPanel = new InfoPanel(gameEngine, gamePanel); // Adicionado gamePanel para comunicação entre as classes.

        root.setCenter(gamePanel);
        root.setRight(infoPanel);

        // Menu sanduíche (hambúrguer) no topo esquerdo
        javafx.scene.control.Button menuButton = new javafx.scene.control.Button("☰");
        menuButton.setStyle("-fx-font-size: 22px; -fx-background-color: #181818; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 8;");

        javafx.scene.control.ContextMenu menuPopup = new javafx.scene.control.ContextMenu();
        javafx.scene.control.MenuItem addJogador = new javafx.scene.control.MenuItem("Adicionar Jogador");
        javafx.scene.control.MenuItem listarJogadores = new javafx.scene.control.MenuItem("Listar Jogadores");
        javafx.scene.control.MenuItem listarPartidas = new javafx.scene.control.MenuItem("Histórico de Partidas");
        javafx.scene.control.MenuItem verRanking = new javafx.scene.control.MenuItem("Ver Ranking");
        menuPopup.getItems().addAll(addJogador, listarJogadores, listarPartidas, verRanking);

        menuButton.setOnAction(e -> {
            menuPopup.show(menuButton, javafx.geometry.Side.BOTTOM, 0, 0);
        });

        // Eventos do menu sanduíche
        addJogador.setOnAction(e -> {
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
            dialog.setTitle("Adicionar Jogador");
            dialog.setHeaderText("Novo Jogador");
            dialog.setContentText("Nome:");
            dialog.showAndWait().ifPresent(nome -> {
                try {
                    playerDAO.addPlayer(new Player(nome, 0));
                    java.util.List<Player> jogadores = playerDAO.getAllPlayers();
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Jogadores");
                    alert.setHeaderText("Lista de Jogadores");
                    String lista = jogadores.stream().map(p -> p.getName() + " - " + p.getScore()).reduce("", (a, b) -> a + "\n" + b);
                    alert.setContentText(lista);
                    alert.showAndWait();
                } catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
            });
        });
        listarJogadores.setOnAction(e -> {
            try {
                java.util.List<Player> jogadores = playerDAO.getAllPlayers();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Jogadores");
                alert.setHeaderText("Lista de Jogadores");
                String lista = jogadores.stream().map(p -> p.getName() + " - " + p.getScore()).reduce("", (a, b) -> a + "\n" + b);
                alert.setContentText(lista);
                alert.showAndWait();
            } catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
        });
        listarPartidas.setOnAction(e -> {
            try {
                java.util.List<Player> jogadores = playerDAO.getAllPlayers();
                Player recordista = jogadores.stream().max(java.util.Comparator.comparingInt(Player::getScore)).orElse(null);
                String recorde = (recordista == null) ? "Nenhum recorde registrado." : recordista.getName() + " - " + recordista.getScore();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Último Recorde");
                alert.setHeaderText("Recorde Atual");
                alert.setContentText(recorde);
                alert.showAndWait();
            } catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
        });
        verRanking.setOnAction(e -> {
            try {
                java.util.List<Player> jogadores = playerDAO.getAllPlayers();
                jogadores.sort(java.util.Comparator.comparingInt(Player::getScore).reversed());
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Ranking");
                alert.setHeaderText("Ranking por Score");
                String ranking = jogadores.stream().map(p -> p.getName() + " - " + p.getScore()).reduce("", (a, b) -> a + "\n" + b);
                alert.setContentText(ranking);
                alert.showAndWait();
            } catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Top bar: título centralizado + botão de alternância de tema (direita)
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(6, 10, 6, 10));
        topBar.setAlignment(Pos.CENTER);

        // Left: Menu sanduíche
        topBar.getChildren().add(menuButton);

        // Título central moderno e simples (com fundo para melhorar visibilidade)
        Label title = new Label("Tetris Jhow");
        try (InputStream fis = getClass().getResourceAsStream("/fonts/cyberpunk.ttf")) {
            if (fis != null) {
                Font loaded = Font.loadFont(fis, 32);
                if (loaded != null) {
                    title.setFont(Font.font(loaded.getFamily(), FontWeight.SEMI_BOLD, 32));
                } else {
                    title.setFont(Font.font("Verdana", FontWeight.SEMI_BOLD, 32));
                }
            } else {
                title.setFont(Font.font("Verdana", FontWeight.SEMI_BOLD, 32));
            }
        } catch (Exception e) {
            e.printStackTrace();
            title.setFont(Font.font("Verdana", FontWeight.SEMI_BOLD, 32));
        }
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(5, Color.BLACK));

        // Espaço entre menu e título
        Region leftSpacer = new Region();
        leftSpacer.setMinWidth(16);
        topBar.getChildren().add(leftSpacer);

        // Título central
        topBar.getChildren().add(title);

        // Right area: theme toggle button
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        Button themeToggle = new Button();
        themeToggle.setFocusTraversable(false);
        themeToggle.setText(Theme.isDark() ? "☀" : "🌙");
        themeToggle.setOnAction(e -> {
            Theme.toggle();
            themeToggle.setText(Theme.isDark() ? "☀" : "🌙");
            gamePanel.requestFocus();
        });
        Theme.darkProperty().addListener(observable -> themeToggle.setText(Theme.isDark() ? "☀" : "🌙"));

        topBar.getChildren().addAll(rightSpacer, themeToggle);
        root.setTop(topBar);

        // 3. Configurar a cena e os controlos
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> {
            // 1. Passa a tecla para o motor do jogo
            gameEngine.handleKeyPress(event.getCode());
            // 2. CONSOME O EVENTO para que o JavaFX não mude o foco
            event.consume();
        });

        // 4. Configurar e exibir a janela principal
        primaryStage.setTitle("TetrisFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        gamePanel.requestFocus();
        primaryStage.show();

        // 5. Iniciar o jogo
        gameEngine.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}