package com.Iviinvest.controller;

import com.Iviinvest.model.CarteiraUsuario;
import com.Iviinvest.model.ObjetivoUsuario;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.service.CarteiraUsuarioService;
import com.Iviinvest.service.ObjetivoUsuarioService;
import com.Iviinvest.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.Data;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carteiras")
public class CarteiraUsuarioController {

    private final CarteiraUsuarioService carteiraService;
    private final UsuarioService usuarioService;
    private final ObjetivoUsuarioService objetivoService;

    @Value("${openapi.api.key}")
    private String openApiKey;

    @Value("${finnhub.api.key}")
    private String finnhubApiKey;

    public CarteiraUsuarioController(CarteiraUsuarioService carteiraService, UsuarioService usuarioService, ObjetivoUsuarioService objetivoService) {
        this.carteiraService = carteiraService;
        this.usuarioService = usuarioService;
        this.objetivoService = objetivoService;
    }

    /**
     * Simular duas carteiras usando IA (OpenAI) e salvar no banco.
     */
    @PostMapping("/simular")
    @Operation(summary = "Simular carteiras completas em duas etapas usando IA", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> simularCarteiras(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        try {
            Usuario usuario = usuarioService.findByEmail(userDetails.getUsername());
            ObjetivoUsuario objetivo = objetivoService.buscarUltimoPorUsuario(usuario)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum objetivo encontrado"));

            // 1️⃣ Gerar distribuição ideal
            String promptDistribuicao = gerarPromptDistribuicao(objetivo);
            JSONObject distribuicao = chamarOpenAI(promptDistribuicao);
            System.out.println("Distribuição sugerida: " + distribuicao.toString(2));

            // 2️⃣ Gerar ativos com base na distribuição
            String promptAtivos = gerarPromptAtivos(objetivo, distribuicao);
            JSONObject ativos = chamarOpenAI(promptAtivos);
            System.out.println("Ativos sugeridos: " + ativos.toString(2));

            // 3️⃣ Extrair carteiras
            JSONObject carteiraConservadora = ativos.getJSONObject("carteiraConservadora");
            JSONObject carteiraAgressiva = ativos.getJSONObject("carteiraAgressiva");

            // 4️⃣ Salvar no banco
            CarteiraUsuario carteira = new CarteiraUsuario();
            carteira.setUsuario(usuario);
            carteira.setObjetivoUsuario(objetivo);
            carteira.setCarteiraConservadoraJson(carteiraConservadora.toString());
            carteira.setCarteiraAgressivaJson(carteiraAgressiva.toString());

            carteiraService.salvar(carteira);

            return ResponseEntity.ok(Map.of(
                    "message", "Carteiras simuladas e salvas com sucesso",
                    "distribuicao", distribuicao.toString(),
                    "carteiraConservadora", carteiraConservadora.toString(),
                    "carteiraAgressiva", carteiraAgressiva.toString()
            ));

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erro ao simular carteiras: " + ex.getMessage()
            ));
        }
    }


    private String gerarPromptDistribuicao(ObjetivoUsuario objetivo) {
        return """
    Para um investidor com o seguinte perfil:
    - Objetivo: %s
    - Prazo: %s
    - Liquidez: %s

    Retorne somente a distribuição percentual ideal em formato JSON entre as categorias:

    {
      "rendaFixa": 40,
      "acoes": 30,
      "fiis": 20,
      "cripto": 10
    }

    Certifique-se de que os valores somam 100.
    """.formatted(
                objetivo.getObjetivo(),
                objetivo.getPrazo(),
                objetivo.getLiquidez()
        );
    }
    private String gerarPromptAtivos(ObjetivoUsuario objetivo, JSONObject distribuicao) {
        return """
    Com base nesta distribuição:

    %s

    E no seguinte perfil:
    - Objetivo: %s
    - Prazo: %s
    - Valor Inicial: R$ %.2f
    - Aporte Mensal: R$ %.2f
    - Patrimônio Atual: R$ %.2f
    - Liquidez: %s
    - Setores a evitar: %s

    Sugira os ativos específicos para compor duas carteiras:
    - carteiraConservadora
    - carteiraAgressiva

    Cada uma deve conter:
    - 5 a 8 ações (ex: PETR4, ITUB4)
    - 5 a 8 FIIs (ex: KNRI11, HGLG11)
    - até 2 criptoativos (BTC, ETH)
    - 1 ou 2 opções de renda fixa

    Responda exclusivamente no formato JSON:

    {
      "carteiraConservadora": {
        "rendaFixa": [...],
        "acoes": [...],
        "fiis": [...],
        "cripto": [...]
      },
      "carteiraAgressiva": {
        "rendaFixa": [...],
        "acoes": [...],
        "fiis": [...],
        "cripto": [...]
      }
    }
    """.formatted(
                distribuicao.toString(2),
                objetivo.getObjetivo(),
                objetivo.getPrazo(),
                objetivo.getValorInicial(),
                objetivo.getAporteMensal(),
                objetivo.getPatrimonioAtual(),
                objetivo.getLiquidez(),
                objetivo.getSetoresEvitar() != null ? objetivo.getSetoresEvitar() : "[]"
        );
    }


    private JSONObject chamarOpenAI(String prompt) throws Exception {
        String body = new JSONObject(Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        )).toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + openApiKey.trim())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status da resposta OpenAI: " + response.statusCode());
        System.out.println("Body da resposta OpenAI: " + response.body());

        JSONObject json = new JSONObject(response.body());
        String content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        try {
            // Remove as crases e identificadores de bloco de código
            String cleaned = content
                    .replaceAll("(?i)^```json\\s*", "")  // remove ```json (no início)
                    .replaceAll("^```\\s*", "")          // ou apenas ``` no início
                    .replaceAll("\\s*```$", "")          // remove ``` no fim
                    .trim();

            return new JSONObject(cleaned);
        } catch (Exception e) {
            System.err.println("Erro ao converter resposta para JSON: " + content);
            throw new RuntimeException("A resposta da IA não está em formato JSON válido. Conteúdo: " + content);
        }
    }






    /**
     * Buscar carteira associada a um objetivo
     */
    @GetMapping("/{objetivoId}")
    @Operation(summary = "Buscar carteira associada a um objetivo", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> buscarCarteiraPorObjetivo(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails,
            @PathVariable Long objetivoId) {

        try {
            Usuario usuario = usuarioService.findByEmail(userDetails.getUsername());
            ObjetivoUsuario objetivo = objetivoService.buscarPorId(objetivoId);

            CarteiraUsuario carteira = carteiraService.buscarPorObjetivo(objetivo)
                    .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Carteira não encontrada para o objetivo."));

            if (!carteira.getUsuario().getId().equals(usuario.getId())) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Usuário não autorizado a acessar essa carteira.");
            }

            return ResponseEntity.ok(Map.of(
                    "carteiraConservadora", carteira.getCarteiraConservadoraJson(),
                    "carteiraAgressiva", carteira.getCarteiraAgressivaJson()
            ));

        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
            ));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "500",
                    "error", "INTERNAL_SERVER_ERROR",
                    "message", "Erro inesperado ao buscar carteira."
            ));
        }
    }

    @Data
    public static class CarteiraRequestDTO {
        private Long objetivoId;
        private String carteiraConservadoraJson;
        private String carteiraAgressivaJson;
    }
}
