package com.Iviinvest.dto;

import java.util.List;
import java.util.Map;

public class CarteiraResponseDTO {
    private Map<String,Integer> percentuais;
    private Map<String,List<AtivoDTO>> ativos;

    public static class AtivoDTO {
        private String nomeAtivo;
        private Double precoUnitario;
        private Integer quantidadeCotas;

        public AtivoDTO(String nomeAtivo, Double precoUnitario, Integer quantidadeCotas) {
            this.nomeAtivo = nomeAtivo;
            this.precoUnitario = precoUnitario;
            this.quantidadeCotas = quantidadeCotas;
        }
        public String getNomeAtivo() { return nomeAtivo; }
        public Double getPrecoUnitario() { return precoUnitario; }
        public Integer getQuantidadeCotas() { return quantidadeCotas; }
    }

    public CarteiraResponseDTO(Map<String,Integer> percentuais,
                               Map<String,List<AtivoDTO>> ativos) {
        this.percentuais = percentuais;
        this.ativos = ativos;
    }
    public Map<String,Integer> getPercentuais() { return percentuais; }
    public Map<String,List<AtivoDTO>> getAtivos() { return ativos; }
}
