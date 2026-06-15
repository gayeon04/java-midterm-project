package com.hotmail.kalebmarc.textfighter.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardClient {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient client = HttpClient.newHttpClient();
    private String token = null;

    public boolean login(String username, String password) throws Exception {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) return false;

        JSONObject json = new JSONObject(response.body());
        this.token = json.getString("token");
        return true;
    }

    public List<String> fetchLeaderboard() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/leaderboard"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        JSONArray rankings = json.getJSONArray("rankings");

        List<String> result = new ArrayList<>();
        for (int i = 0; i < rankings.length(); i++) {
            JSONObject entry = rankings.getJSONObject(i);
            int rank     = entry.getInt("rank");
            String name  = entry.getString("playerName");
            int level    = entry.getInt("level");
            int kills    = entry.getInt("totalKills");
            int xp       = entry.getInt("xp");
            result.add(rank + "위  " + name + "  Lv." + level + "  킬 " + kills + "  XP " + xp);
        }
        return result;
    }
}
