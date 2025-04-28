package com.Iviinvest.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ObjetivoUsuarioDTO {
    private String objetivo;
    private String prazo;
    private Double valorInicial;
    private Double aporteMensal;
    private Double patrimonioAtual;
    private String liquidez;
    private List<String> setoresEvitar;
}
