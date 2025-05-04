package com.Iviinvest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
public class ObjetivoUsuarioDTO {
    private String objetivo;
    private Integer prazo;
    private Double valorInicial;
    private Double aporteMensal;
    private Double patrimonioAtual;
    private String liquidez;
    private List<String> setoresEvitar;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataCriacao;
}
