package com.Iviinvest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para representar o perfil de investidor de um usuário.
 * <p>
 * Contém informações sobre o perfil de risco do investidor (conservador, moderado, agressivo, etc.).
 * <p>
 * DTO (Data Transfer Object) to represent an investor's risk profile.
 * Contains information about the investor's risk profile (conservative, moderate, aggressive, etc.).
 */
@Schema(description = "DTO que representa o perfil de investidor | DTO representing investor profile")
public class PerfilDTO {

    /**
     * Tipo de perfil de investidor.
     * <p>
     * Exemplos: "CONSERVADOR", "MODERADO", "AGRESSIVO"
     * <p>
     * Investor profile type.
     * Examples: "CONSERVATIVE", "MODERATE", "AGGRESSIVE"
     */
    @Schema(
            description = "Tipo de perfil de investidor | Investor profile type",
            example = "MODERADO",
            allowableValues = {"CONSERVADOR", "MODERADO", "AGRESSIVO"}
    )
    private String perfilInvestidor;

    /**
     * Construtor padrão sem argumentos.
     * <p>
     * Default no-args constructor.
     */
    public PerfilDTO() {}

    /**
     * Construtor com parâmetro para perfil de investidor.
     *
     * @param perfilInvestidor O tipo de perfil de investidor
     *
     * Constructor with investor profile parameter.
     *
     * @param perfilInvestidor The investor profile type
     */
    public PerfilDTO(String perfilInvestidor) {
        this.perfilInvestidor = perfilInvestidor;
    }

    /**
     * Obtém o perfil de investidor.
     *
     * @return O tipo de perfil de investidor
     *
     * Gets the investor profile.
     *
     * @return The investor profile type
     */
    public String getPerfilInvestidor() {
        return perfilInvestidor;
    }

    /**
     * Define o perfil de investidor.
     *
     * @param perfilInvestidor O tipo de perfil de investidor a ser definido
     *
     * Sets the investor profile.
     *
     * @param perfilInvestidor The investor profile type to set
     */
    public void setPerfilInvestidor(String perfilInvestidor) {
        this.perfilInvestidor = perfilInvestidor;
    }
}