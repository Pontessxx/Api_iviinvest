package com.Iviinvest.service;

import com.Iviinvest.model.ObjetivoUsuario;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class IAService {

    @Value("${openapi.api.key}")
    private String openApiKey;

    public JSONObject chamarOpenAI(String prompt) throws Exception {
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

        String content = new JSONObject(response.body())
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        try {
            String cleaned = content
                    .replaceAll("(?i)^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("\\s*```$", "")
                    .trim();

            return new JSONObject(cleaned);
        } catch (Exception e) {
            throw new RuntimeException("Resposta inválida da IA: " + content);
        }
    }

    public String gerarPromptDistribuicao(ObjetivoUsuario objetivo) {
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
        
        Gere DUAS carteiras de investimento:
        - **Carteira Conservadora**: com foco em segurança, liquidez e preservação do capital.
        - **Carteira Agressiva**: com foco em crescimento e maior exposição ao risco.
        
        Distribua o capital entre os quatro segmentos acima com base em princípios de finanças pessoais, incluindo:
        - Capacidade de risco (relacionada ao valor investido e ao patrimônio atual)
        - Necessidade de liquidez (curto prazo prioriza renda fixa)
        - Potencial de diversificação (evitar excesso de ativos em carteiras pequenas)
        
        **Importante:**
        - A soma total deve ser 100%%.
        - Não precisa incluir todos os segmentos se não for recomendado pelo perfil.
        - Não inclua explicações, apenas retorne o JSON com a distribuição percentual ideal.
        - Não recomende de forma alguma BCFF11,
        - as chaves do JSON é conservadora e agressiva assim como no formato abaixo.
        
        Formato:
        
        {
            "conservadora":{
              "rendaFixa": 00,
              "acoes": 00,
              "fiis": 00,
              "cripto": 00
            },
            "agressiva":{
              "rendaFixa": 00,
              "acoes": 00,
              "fiis": 00,
              "cripto": 00
            }
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

    public String gerarPromptAtivos(ObjetivoUsuario objetivo, JSONObject distribuicao, String tipoCarteira) {
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
        
        Gere UMA carteira de investimento %s:
        
        A composição de cada carteira deve conter ativos dos seguintes segmentos: renda fixa, ações, FIIs e criptoativos (caso compatível com o perfil).
        A quantidade de ativos em cada segmento deve ser determinada com base no perfil e objetivos do investidor,
        seguindo boas práticas financeiras como diversificação, adequação ao prazo e tolerância ao risco.
        
        **Regras obrigatórias:**
        - NÃO incluir ativos dos setores listados em "Setores a evitar".
        - A composição deve refletir o alinhamento com o objetivo financeiro declarado.
        - Se o perfil ou objetivo indicar, omita categorias como criptoativos.
        - O ticker deve estar exatamente como é negociado na bolsa, por exemplo: "PETR4", "BOVA11", "KNRI11", "IRBR3".
        
        Responda SOMENTE com um JSON válido no formato abaixo, sem comentários e com a chave carteira:
        
        {
          "carteira": {
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
                objetivo.getSetoresEvitar() != null ? objetivo.getSetoresEvitar() : "[]",
                tipoCarteira
        );
    }
}
