package com.Iviinvest.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class PrecoAtivoService {

    @Value("${brapi.api.key}")
    private String brapiApiKey;

    public double buscarPreco(String simbolo) {
        try {
            String url = "https://brapi.dev/api/quote/" + simbolo.toUpperCase();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + brapiApiKey.trim())
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            JSONArray results = json.optJSONArray("results");

            if (results != null && results.length() > 0) {
                JSONObject ativo = results.getJSONObject(0);
                return ativo.optDouble("regularMarketPrice", 0.0);
            }

            System.err.println("Ticker não encontrado ou sem preço: " + simbolo);
            return 0.0;

        } catch (Exception e) {
            System.err.println("Erro ao buscar preço de " + simbolo + ": " + e.getMessage());
            return 0.0;
        }
    }
}
