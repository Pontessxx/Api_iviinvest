package com.Iviinvest.dto;

import jakarta.validation.constraints.*;
import java.util.Map;

public class AtualizaDistribuicaoDTO {

    @NotBlank
    private String tipoCarteira;              // "conservadora" ou "agressiva"

    @NotEmpty
    private Map<@Pattern(regexp="rendaFixa|acoes|fiis|cripto") String,
                @Min(0) @Max(100) Integer> distribuicao;

    public String getTipoCarteira() {
        return tipoCarteira;
    }

    public void setTipoCarteira(String tipoCarteira) {
        this.tipoCarteira = tipoCarteira;
    }

    public Map<String,
            Integer> getDistribuicao() {
        return distribuicao;
    }

    public void setDistribuicao(Map<String,
            Integer> distribuicao) {
        this.distribuicao = distribuicao;
    }
}
