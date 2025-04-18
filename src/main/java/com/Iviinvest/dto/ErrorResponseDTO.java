package com.Iviinvest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Formato de erro retornado pela API")
public class ErrorResponseDTO {

    @Schema(description = "Código de status HTTP", example = "401")
    private String status;

    @Schema(description = "Tipo de erro HTTP", example = "401 UNAUTHORIZED")
    private String error;

    @Schema(description = "Mensagem descritiva do erro", example = "Senha incorreta")
    private String message;

    public ErrorResponseDTO(String status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public String getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
}
