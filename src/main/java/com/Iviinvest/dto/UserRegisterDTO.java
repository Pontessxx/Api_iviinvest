package com.Iviinvest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para cadastro de novo usuário")
public class UserRegisterDTO {

    @Schema(description = "Email do novo usuário", example = "novo@email.com")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "Senha para o novo usuário", example = "senhaSegura123")
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
