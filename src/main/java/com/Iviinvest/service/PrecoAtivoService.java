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
            // limpa espaços e força maiúsculas
            String clean = simbolo.trim().toUpperCase().replaceAll("\\s+","");

            // ou, para garantir encoding:
            // String clean = URLEncoder.encode(simbolo.trim(), StandardCharsets.UTF_8);

            String url = "https://brapi.dev/api/quote/" + clean;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + brapiApiKey.trim())
                    .GET()
                    .build();

            HttpResponse<String> resp = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(resp.body());
            JSONArray results = json.optJSONArray("results");
            if (results != null && results.length()>0) {
                return results.getJSONObject(0)
                        .optDouble("regularMarketPrice", 0.0);
            }
            System.err.println("Ticker não encontrado ou sem preço: " + clean);
        } catch(Exception e){
            System.err.println("Erro ao buscar preço de " + simbolo + ": " + e.getMessage());
        }
        return 0.0;
    }
}
