package com.Iviinvest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de login do usuário")
public class LoginDTO {

    @Schema(description = "Email do usuário", example = "usuario@email.com")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "Senha do usuário", example = "123456")
    @NotBlank
    private String senha;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
