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

import com.Iviinvest.model.CarteiraAtivo;
import com.Iviinvest.model.CarteiraPercentual;
import com.Iviinvest.repository.CarteiraAtivoRepository;
import com.Iviinvest.repository.CarteiraPercentualRepository;



@RestController
@RequestMapping("/api/carteiras")
public class CarteiraUsuarioController {

    private final CarteiraUsuarioService carteiraService;
    private final UsuarioService usuarioService;
    private final ObjetivoUsuarioService objetivoService;

    @Value("${openapi.api.key}")
    private String openApiKey;

    @Value("${brapi.api.key}")
    private String brapiApiKey;


    private final CarteiraAtivoRepository carteiraAtivoRepository;
    private final CarteiraPercentualRepository carteiraPercentualRepository;

    public CarteiraUsuarioController(
            CarteiraUsuarioService carteiraService,
            UsuarioService usuarioService,
            ObjetivoUsuarioService objetivoService,
            CarteiraAtivoRepository carteiraAtivoRepository,
            CarteiraPercentualRepository carteiraPercentualRepository) {

        this.carteiraService = carteiraService;
        this.usuarioService = usuarioService;
        this.objetivoService = objetivoService;
        this.carteiraAtivoRepository = carteiraAtivoRepository;
        this.carteiraPercentualRepository = carteiraPercentualRepository;
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

            if (carteiraPercentualRepository.existsByUsuarioIdAndObjetivoId(usuario.getId(), objetivo.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "message", "Já existe uma simulação para este objetivo. Por favor, exclua a simulação antes de criar uma nova."
                ));
            }
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

            // 4️⃣ Salvar no banco principal (JSON)
            CarteiraUsuario carteira = carteiraService.buscarPorObjetivo(objetivo)
                    .orElse(new CarteiraUsuario());

            carteira.setUsuario(usuario);
            carteira.setObjetivoUsuario(objetivo);
            carteira.setCarteiraConservadoraJson(carteiraConservadora.toString());
            carteira.setCarteiraAgressivaJson(carteiraAgressiva.toString());

            carteiraService.salvar(carteira);

            // 5️⃣ Salvar percentuais por segmento
            JSONArray segmentos = distribuicao.names();
            for (int i = 0; i < segmentos.length(); i++) {
                String segmento = segmentos.getString(i);
                int percentual = distribuicao.getInt(segmento);

                CarteiraPercentual cp = new CarteiraPercentual();
                cp.setSegmento(segmento);
                cp.setPercentual(percentual);
                cp.setUsuario(usuario);
                cp.setObjetivo(objetivo);
                carteiraPercentualRepository.save(cp);
            }

            // 6️⃣ Salvar ativos das carteiras
            salvarAtivosCarteira("conservadora", carteiraConservadora, usuario, objetivo, distribuicao);
            salvarAtivosCarteira("agressiva", carteiraAgressiva, usuario, objetivo, distribuicao);

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

    private void salvarAtivosCarteira(
            String tipo,
            JSONObject carteiraJson,
            Usuario usuario,
            ObjetivoUsuario objetivo,
            JSONObject distribuicao
    ) {
        String nomeCarteira = objetivo.getObjetivo() + " - " + objetivo.getPrazo();
        double valorTotal = objetivo.getValorInicial();

        for (String segmento : carteiraJson.keySet()) {

            // ⚠️ Pula o segmento de renda fixa totalmente
            if (segmento.equalsIgnoreCase("rendaFixa")) {
                System.out.println("⏭️ Ignorando ativos de renda fixa para o segmento: " + segmento);
                continue;
            }

            JSONArray ativos = carteiraJson.getJSONArray(segmento);
            int percentualSegmento = distribuicao.optInt(segmento, 0);
            double valorSegmento = valorTotal * percentualSegmento / 100.0;

            int qtdAtivos = ativos.length();
            double valorPorAtivo = qtdAtivos > 0 ? valorSegmento / qtdAtivos : 0;

            for (int i = 0; i < qtdAtivos; i++) {
                String nomeAtivo = ativos.getString(i);
                double precoUnitario = buscarPrecoDoAtivo(nomeAtivo);

                int quantidadeCotas = precoUnitario > 0
                        ? (int) Math.floor(valorPorAtivo / precoUnitario)
                        : 0;

                if (quantidadeCotas <= 0) continue;

                CarteiraAtivo ca = new CarteiraAtivo();
                ca.setSegmento(segmento);
                ca.setTipoCarteira(nomeCarteira);
                ca.setNomeAtivo(nomeAtivo);
                ca.setUsuario(usuario);
                ca.setObjetivo(objetivo);
                ca.setPrecoUnitario(precoUnitario);
                ca.setQuantidadeCotas(quantidadeCotas);

                carteiraAtivoRepository.save(ca);
            }
        }
    }



    private double buscarPrecoDoAtivo(String simbolo) {
        try {
            // Aqui NÃO adicionamos ".SA" esperamos que o símbolo venha corretamente
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







    private String gerarPromptDistribuicao(ObjetivoUsuario objetivo) {
        return """
Você é um assistente financeiro que deve calcular a distribuição percentual ideal para os segmentos de investimento:

- rendaFixa
- acoes
- fiis
- cripto

Leve em conta o perfil e a capacidade financeira do investidor a seguir:

- Objetivo: %s
- Prazo: %s
- Liquidez: %s
- Valor Inicial: R$ %.2f
- Aporte Mensal: R$ %.2f
- Patrimônio Atual: R$ %.2f

Distribua o capital entre os quatro segmentos acima com base em princípios de finanças pessoais, incluindo:
- Capacidade de risco (relacionada ao valor investido e ao patrimônio atual)
- Necessidade de liquidez (curto prazo prioriza renda fixa)
- Potencial de diversificação (evitar excesso de ativos em carteiras pequenas)

**Importante:**
- A soma total deve ser 100%%.
- Não precisa incluir todos os segmentos se não for recomendado pelo perfil.
- Não inclua explicações, apenas retorne o JSON com a distribuição percentual ideal.

Formato:

{
  "rendaFixa": 00,
  "acoes": 00,
  "fiis": 00,
  "cripto": 00
}
""".formatted(
                objetivo.getObjetivo(),
                objetivo.getPrazo(),
                objetivo.getLiquidez(),
                objetivo.getValorInicial(),
                objetivo.getAporteMensal(),
                objetivo.getPatrimonioAtual()
        );
    }
    private String gerarPromptAtivos(ObjetivoUsuario objetivo, JSONObject distribuicao) {
        return """
        Você é um assistente financeiro inteligente especializado em Quality Diversity (QD),
        capaz de gerar múltiplas carteiras distintas, porém igualmente válidas, para perfis de investimento.
        
        Com base nesta distribuição ideal entre segmentos:
        
        %s
        
        E nas características deste investidor:
        - Objetivo financeiro: %s
        - Prazo do investimento: %s
        - Valor Inicial: R$ %.2f
        - Aporte Mensal: R$ %.2f
        - Patrimônio Atual: R$ %.2f
        - Preferência de Liquidez: %s
        - Setores a evitar: %s
        
        Gere DUAS carteiras de investimento:
        - **Carteira Conservadora**: com foco em segurança, liquidez e preservação do capital.
        - **Carteira Agressiva**: com foco em crescimento e maior exposição ao risco.
        
        A composição de cada carteira deve conter ativos dos seguintes segmentos: renda fixa, ações, FIIs e criptoativos (caso compatível com o perfil).
        A quantidade de ativos em cada segmento deve ser determinada com base no perfil e objetivos do investidor,
        seguindo boas práticas financeiras como diversificação, adequação ao prazo e tolerância ao risco.
        
        **Regras obrigatórias:**
        - NÃO incluir ativos dos setores listados em "Setores a evitar".
        - A composição deve refletir o alinhamento com o objetivo financeiro declarado.
        - Se o perfil ou objetivo indicar, omita categorias como criptoativos.
        - O ticker deve estar exatamente como é negociado na bolsa, por exemplo: "PETR4", "BOVA11", "KNRI11", "IRBR3".
        
        Responda SOMENTE com um JSON válido no formato abaixo, sem comentários:
        
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

    @DeleteMapping("/{objetivoId}")
    @Operation(summary = "Excluir simulação de carteira por objetivo", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> deletarSimulacaoPorObjetivo(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails,
            @PathVariable Long objetivoId) {
        try {
            Usuario usuario = usuarioService.findByEmail(userDetails.getUsername());
            ObjetivoUsuario objetivo = objetivoService.buscarPorId(objetivoId);

            // Deleta ativos e percentuais
            List<CarteiraAtivo> ativos = carteiraAtivoRepository.findAll().stream()
                    .filter(a -> a.getUsuario().getId().equals(usuario.getId()) &&
                            a.getObjetivo().getId().equals(objetivo.getId()))
                    .toList();
            carteiraAtivoRepository.deleteAll(ativos);

            List<CarteiraPercentual> percentuais = carteiraPercentualRepository.findAll().stream()
                    .filter(p -> p.getUsuario().getId().equals(usuario.getId()) &&
                            p.getObjetivo().getId().equals(objetivo.getId()))
                    .toList();
            carteiraPercentualRepository.deleteAll(percentuais);

            // Deleta JSON da carteira, se existir
            carteiraService.buscarPorObjetivo(objetivo).ifPresent(carteiraService::excluir);

            return ResponseEntity.ok(Map.of("message", "Simulação removida com sucesso"));

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erro ao excluir simulação: " + ex.getMessage()
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
