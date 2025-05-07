package com.Iviinvest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para receber a pergunta do usuário sobre explicação da carteira.
 */
@Data
public class ChatRequestDTO {
    @NotBlank
    private String question;
}
