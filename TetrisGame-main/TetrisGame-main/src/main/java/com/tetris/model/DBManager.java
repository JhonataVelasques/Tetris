package com.tetris.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private static final String DB_URL = "jdbc:sqlite:tetris.db";

    public static void initDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS jogador (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS partida (id INTEGER PRIMARY KEY AUTOINCREMENT, jogador_id INTEGER, score INTEGER, data TEXT, FOREIGN KEY(jogador_id) REFERENCES jogador(id))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void adicionarJogador(String nome) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO jogador (nome) VALUES (?)");
            ps.setString(1, nome);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> listarJogadores() {
        List<String> jogadores = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT nome FROM jogador");
            while (rs.next()) {
                jogadores.add(rs.getString("nome"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jogadores;
    }

    public static void registrarPartida(int jogadorId, int score, String data) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO partida (jogador_id, score, data) VALUES (?, ?, ?)");
            ps.setInt(1, jogadorId);
            ps.setInt(2, score);
            ps.setString(3, data);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> listarPartidas() {
        List<String> partidas = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT p.id, j.nome, p.score, p.data FROM partida p JOIN jogador j ON p.jogador_id = j.id ORDER BY p.score DESC");
            while (rs.next()) {
                partidas.add(rs.getString("nome") + " - " + rs.getInt("score") + " pts em " + rs.getString("data"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return partidas;
    }
}
