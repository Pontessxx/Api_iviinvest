package com.Iviinvest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição de redefinição de senha com token")
public class ResetPasswordDTO {

    @Schema(description = "Token de redefinição", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotBlank
    private String token;

    @Schema(description = "Nova senha do usuário", example = "novaSenha@2025")
    @NotBlank
    private String newPassword;

    // Getters e Setters

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
