package com.Iviinvest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) para credenciais de autenticação do usuário.
 * <p>
 * Contém os dados necessários para o processo de login: e-mail e senha.
 * Todos os campos são obrigatórios e validados conforme regras de negócio.
 * <p>
 * DTO (Data Transfer Object) for user authentication credentials.
 * Contains the required data for login process: email and password.
 * All fields are mandatory and validated according to business rules.
 */
@Schema(
        description = "Credenciais de login do usuário | User login credentials"
)
public class LoginDTO {

    /**
     * Endereço de e-mail do usuário.
     * <p>
     * Deve ser um e-mail válido e não pode estar em branco.
     * <p>
     * User email address.
     * Must be a valid email and cannot be blank.
     */
    @Schema(
            description = "Endereço de e-mail válido do usuário | User's valid email address",
            example = "usuario@email.com"
    )
    @Email(message = "O e-mail deve ser um endereço válido | Email must be a valid address")
    @NotBlank(message = "O e-mail não pode estar em branco | Email cannot be blank")
    private String email;

    /**
     * Senha de acesso do usuário.
     * <p>
     * Não pode estar em branco. A complexidade deve ser tratada no front-end.
     * <p>
     * User password.
     * Cannot be blank. Complexity should be handled at front-end.
     */
    @Schema(
            description = "Senha de acesso do usuário | User password",
            example = "123456"
    )
    @NotBlank(message = "A senha não pode estar em branco | Password cannot be blank")
    private String senha;

    // Métodos de acesso (Getters e Setters) | Access methods (Getters and Setters)

    /**
     * Obtém o e-mail do usuário.
     *
     * @return Endereço de e-mail
     *
     * Gets the user's email.
     *
     * @return Email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o e-mail do usuário.
     *
     * @param email Endereço de e-mail válido
     *
     * Sets the user's email.
     *
     * @param email Valid email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtém a senha do usuário.
     *
     * @return Senha de acesso
     *
     * Gets the user's password.
     *
     * @return Access password
     */
    public String getSenha() {
        return senha;
    }

    /**
     * Define a senha do usuário.
     *
     * @param senha Senha de acesso
     *
     * Sets the user's password.
     *
     * @param senha Access password
     */
    public void setSenha(String senha) {
        this.senha = senha;
    }
}