package com.tetris.model.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tetris.model.Player;

public class SqlServerPlayerDAO {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=TetrisDB;encrypt=true;trustServerCertificate=true";
    private static final String USER = "seu_usuario"; // Altere para seu usuário
    private static final String PASSWORD = "sua_senha"; // Altere para sua senha

    public void addPlayer(Player player) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "INSERT INTO Player (name, score) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, player.getName());
                stmt.setInt(2, player.getScore());
                stmt.executeUpdate();
            }
        }
    }

    public List<Player> getAllPlayers() throws SQLException {
        List<Player> players = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM Player";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Player p = new Player(rs.getString("name"), rs.getInt("score"));
                    players.add(p);
                }
            }
        }
        return players;
    }
}
