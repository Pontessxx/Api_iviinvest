package com.Iviinvest.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Representa um ponto da série temporal de rentabilidade.
 * Ex.: período 1 → mês/ano 1, valor → % ou valor do patrimônio.
 */
@Getter
@Setter
public class PontoDTO {
    private Integer periodo;
    private Double valor;

    public PontoDTO() {}

    public PontoDTO(Integer periodo, Double valor) {
        this.periodo = periodo;
        this.valor = valor;
    }
}
