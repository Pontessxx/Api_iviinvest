package com.Iviinvest.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Agrega dados da simulação: objetivo, distribuição,
 * lista de ativos e série temporal de rentabilidade.
 */
@Getter
@Setter
public class SimulacaoDTO {
    private ObjetivoUsuarioDTO objetivo;
    private Map<String, Integer> percentuais;
    private Map<String, List<CarteiraResponseDTO.AtivoDTO>> ativos;
    private List<PontoDTO> grafico;

    public SimulacaoDTO() {}

    public SimulacaoDTO(ObjetivoUsuarioDTO objetivo,
                        Map<String, Integer> percentuais,
                        Map<String, List<CarteiraResponseDTO.AtivoDTO>> ativos,
                        List<PontoDTO> grafico) {
        this.objetivo     = objetivo;
        this.percentuais  = percentuais;
        this.ativos       = ativos;
        this.grafico      = grafico;
    }
}
