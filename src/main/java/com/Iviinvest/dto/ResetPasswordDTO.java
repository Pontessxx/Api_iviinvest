package com.Iviinvest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) para requisição de redefinição de senha com token.
 * <p>
 * Contém o token de redefinição e a nova senha a ser configurada.
 * Todos os campos são obrigatórios e validados conforme regras de negócio.
 * <p>
 * DTO (Data Transfer Object) for password reset request with token.
 * Contains the reset token and the new password to be set.
 * All fields are mandatory and validated according to business rules.
 */
@Schema(
        description = "DTO para requisição de redefinição de senha com token | DTO for password reset request with token"
)
public class ResetPasswordDTO {

    /**
     * Token único de redefinição de senha.
     * <p>
     * Deve ser um UUID válido e não pode estar em branco.
     * <p>
     * Unique password reset token.
     * Must be a valid UUID and cannot be blank.
     */
    @Schema(
            description = "Token único de redefinição gerado pelo sistema | System-generated unique reset token",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @NotBlank(message = "O token de redefinição é obrigatório | Reset token is required")
    private String token;

    /**
     * Nova senha de acesso do usuário.
     * <p>
     * Não pode estar em branco. Recomenda-se complexidade adequada.
     * <p>
     * User's new password.
     * Cannot be blank. Adequate complexity is recommended.
     */
    @Schema(
            description = "Nova senha de acesso do usuário | User's new password",
            example = "novaSenha@2025",
            minLength = 8,
            pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    )
    @NotBlank(message = "A nova senha é obrigatória | New password is required")
    private String newPassword;

    // Métodos de acesso (Getters e Setters) | Access methods (Getters and Setters)

    /**
     * Obtém o token de redefinição.
     *
     * @return Token de redefinição
     *
     * Gets the reset token.
     *
     * @return Reset token
     */
    public String getToken() {
        return token;
    }

    /**
     * Define o token de redefinição.
     *
     * @param token Token de redefinição válido
     *
     * Sets the reset token.
     *
     * @param token Valid reset token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Obtém a nova senha.
     *
     * @return Nova senha
     *
     * Gets the new password.
     *
     * @return New password
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Define a nova senha.
     *
     * @param newPassword Nova senha a ser definida
     *
     * Sets the new password.
     *
     * @param newPassword New password to be set
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}