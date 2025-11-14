package com.tetris.model;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class LocalStorageManager {
    private static final String STORAGE_PATH = "src/main/resources/storage/";

    public static void saveJogadores(List<String> jogadores) {
        JSONArray arr = new JSONArray(jogadores);
        writeFile("jogadores.json", arr.toString(2));
    }

    public static List<String> loadJogadores() {
        String json = readFile("jogadores.json");
        if (json == null || json.isEmpty()) return new ArrayList<>();
        JSONArray arr = new JSONArray(json);
        List<String> jogadores = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) jogadores.add(arr.getString(i));
        return jogadores;
    }

    public static void saveRecorde(String recorde) {
        writeFile("recorde.json", new JSONObject().put("recorde", recorde).toString(2));
    }

    public static String loadRecorde() {
        String json = readFile("recorde.json");
        if (json == null || json.isEmpty()) return "Nenhum recorde registrado.";
        JSONObject obj = new JSONObject(json);
        return obj.optString("recorde", "Nenhum recorde registrado.");
    }

    public static void saveRanking(List<String> ranking) {
        JSONArray arr = new JSONArray(ranking);
        writeFile("ranking.json", arr.toString(2));
    }

    public static List<String> loadRanking() {
        String json = readFile("ranking.json");
        if (json == null || json.isEmpty()) return new ArrayList<>();
        JSONArray arr = new JSONArray(json);
        List<String> ranking = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) ranking.add(arr.getString(i));
        return ranking;
    }

    private static void writeFile(String fileName, String content) {
        try {
            Files.write(Paths.get(STORAGE_PATH + fileName), content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(STORAGE_PATH + fileName)));
        } catch (IOException e) {
            return null;
        }
    }
}
