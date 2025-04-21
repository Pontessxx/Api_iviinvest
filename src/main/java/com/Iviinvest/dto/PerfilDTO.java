package com.Iviinvest.dto;

public class PerfilDTO {
    private String perfilInvestidor;

    public PerfilDTO() {}

    public PerfilDTO(String perfilInvestidor) {
        this.perfilInvestidor = perfilInvestidor;
    }

    public String getPerfilInvestidor() {
        return perfilInvestidor;
    }

    public void setPerfilInvestidor(String perfilInvestidor) {
        this.perfilInvestidor = perfilInvestidor;
    }
}
