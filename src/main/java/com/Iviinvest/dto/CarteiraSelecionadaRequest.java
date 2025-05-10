// src/main/java/com/Iviinvest/dto/CarteiraSelecionadaRequest.java
package com.Iviinvest.dto;

import java.util.List;
import java.util.Map;

public class CarteiraSelecionadaRequest {
    // distribuição de percentuais para cada tipo de carteira
    private Map<String, Integer> conservadora;
    private Map<String, Integer> agressiva;
    // ativos escolhidos pelo usuário para cada tipo
    private Map<String, List<String>> ativosConservadora;
    private Map<String, List<String>> ativosAgressiva;
    // getters e setters
    public Map<String, Integer> getConservadora() { return conservadora; }
    public void setConservadora(Map<String, Integer> conservadora) { this.conservadora = conservadora; }
    public Map<String, Integer> getAgressiva() { return agressiva; }
    public void setAgressiva(Map<String, Integer> agressiva) { this.agressiva = agressiva; }
    public Map<String, List<String>> getAtivosConservadora() { return ativosConservadora; }
    public void setAtivosConservadora(Map<String, List<String>> ativosConservadora) { this.ativosConservadora = ativosConservadora; }
    public Map<String, List<String>> getAtivosAgressiva() { return ativosAgressiva; }
    public void setAtivosAgressiva(Map<String, List<String>> ativosAgressiva) { this.ativosAgressiva = ativosAgressiva; }
}
