package com.tetris;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tetris.model.DBManager;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LocalHttpServer {
    private HttpServer server;

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/jogadores", new JogadoresHandler());
        server.createContext("/partidas", new PartidasHandler());
        server.createContext("/ranking", new RankingHandler());
        server.setExecutor(null); // default executor
        server.start();
        System.out.println("Servidor HTTP iniciado em http://localhost:" + port);
    }

    static class JogadoresHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<String> jogadores = DBManager.listarJogadores();
            String json = toJsonArray(jogadores);
            sendResponse(exchange, json);
        }
    }

    static class PartidasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<String> partidas = DBManager.listarPartidas();
            String json = toJsonArray(partidas);
            sendResponse(exchange, json);
        }
    }

    static class RankingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<String> partidas = DBManager.listarPartidas();
            String json = toJsonArray(partidas);
            sendResponse(exchange, json);
        }
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String toJsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append('"').append(list.get(i).replace("\"", "\\\"")).append('"');
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
