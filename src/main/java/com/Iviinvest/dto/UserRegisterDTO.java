package com.Iviinvest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) para cadastro de novos usuários no sistema.
 * <p>
 * Contém as informações mínimas necessárias para registrar um novo usuário:
 * e-mail válido e senha de acesso. Ambos campos são obrigatórios.
 * <p>
 * DTO (Data Transfer Object) for new user registration in the system.
 * Contains the minimum required information to register a new user:
 * valid email and password. Both fields are mandatory.
 */
@Schema(
        description = "DTO para cadastro de novo usuário | DTO for new user registration"
)
public class UserRegisterDTO {

    /**
     * Endereço de e-mail do novo usuário.
     * <p>
     * Deve seguir o formato padrão de e-mails (user@domain.com)
     * e não pode estar em branco.
     * <p>
     * New user's email address.
     * Must follow standard email format (user@domain.com)
     * and cannot be blank.
     */
    @Schema(
            description = "Endereço de e-mail válido para cadastro | Valid email address for registration",
            example = "novo@email.com"
    )
    @Email(message = "O e-mail deve estar em um formato válido | Email must be in a valid format")
    @NotBlank(message = "O e-mail não pode estar em branco | Email cannot be blank")
    private String email;

    /**
     * Senha de acesso para o novo usuário.
     * <p>
     * Não pode estar em branco. Recomenda-se senha forte com:
     * - Mínimo de 8 caracteres
     * - Letras maiúsculas e minúsculas
     * - Números
     * - Caracteres especiais
     * <p>
     * Access password for the new user.
     * Cannot be blank. Strong password recommended with:
     * - Minimum 8 characters
     * - Upper and lower case letters
     * - Numbers
     * - Special characters
     */
    @Schema(
            description = "Senha de acesso segura | Secure access password",
            example = "SenhaSegura@123",
            minLength = 8,
            pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
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