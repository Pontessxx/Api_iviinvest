package com.Iviinvest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para representação pública de informações de usuário.
 * <p>
 * Contém apenas dados não sensíveis que podem ser expostos publicamente.
 * Atualmente inclui apenas o ID do usuário, mas pode ser estendido conforme necessário.
 * <p>
 * DTO (Data Transfer Object) for public representation of user information.
 * Contains only non-sensitive data that can be publicly exposed.
 * Currently includes only the user ID, but can be extended as needed.
 */
@Schema(description = "DTO para informações públicas do usuário | DTO for public user information")
public class UsuarioPublicDTO {

    /**
     * Identificador único do usuário no sistema.
     * <p>
     * ID único gerado pelo banco de dados.
     * <p>
     * Unique user identifier in the system.
     * Database-generated unique ID.
     */
    @Schema(
            description = "ID único do usuário | Unique user ID",
            example = "123"
    )
    private final Long id;

    /**
     * Construtor que recebe o ID do usuário.
     *
     * @param id Identificador único do usuário
     *
     * Constructor that receives the user ID.
     *
     * @param id Unique user identifier
     */
    public UsuarioPublicDTO(Long id) {
        this.id = id;
    }

    /**
     * Obtém o ID do usuário.
     *
     * @return Identificador único do usuário
     *
     * Gets the user ID.
     *
     * @return Unique user identifier
     */
    public Long getId() {
        return id;
    }
}