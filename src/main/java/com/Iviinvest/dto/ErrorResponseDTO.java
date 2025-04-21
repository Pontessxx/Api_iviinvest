package com.Iviinvest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para respostas de erro padronizadas da API.
 * <p>
 * Contém informações estruturadas sobre erros que ocorrem durante as requisições,
 * incluindo código de status, tipo de erro e mensagem descritiva.
 * <p>
 * DTO (Data Transfer Object) for standardized API error responses.
 * Contains structured information about errors that occur during requests,
 * including status code, error type, and descriptive message.
 */
@Schema(
        description = "Formato padrão para respostas de erro da API | Standard API error response format"
)
public class ErrorResponseDTO {

    /**
     * Código de status HTTP numérico.
     * <p>
     * Exemplo: "401"
     * <p>
     * Numeric HTTP status code.
     * Example: "401"
     */
    @Schema(
            description = "Código de status HTTP | HTTP status code",
            example = "401"
    )
    private final String status;

    /**
     * Descrição textual do tipo de erro HTTP.
     * <p>
     * Exemplo: "401 UNAUTHORIZED"
     * <p>
     * Textual description of HTTP error type.
     * Example: "401 UNAUTHORIZED"
     */
    @Schema(
            description = "Tipo do erro HTTP | HTTP error type",
            example = "401 UNAUTHORIZED"
    )
    private final String error;

    /**
     * Mensagem descritiva sobre o erro ocorrido.
     * <p>
     * Exemplo: "Credenciais inválidas"
     * <p>
     * Descriptive message about the error that occurred.
     * Example: "Invalid credentials"
     */
    @Schema(
            description = "Mensagem descritiva do erro | Error description message",
            example = "Senha incorreta | Incorrect password"
    )
    private final String message;

    /**
     * Construtor para criar uma resposta de erro padronizada.
     *
     * @param status Código de status HTTP
     * @param error Tipo de erro HTTP
     * @param message Mensagem descritiva do erro
     *
     * Constructor to create a standardized error response.
     *
     * @param status HTTP status code
     * @param error HTTP error type
     * @param message Error description message
     */
    public ErrorResponseDTO(String status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    // Métodos de acesso (Getters) | Access methods (Getters)

    /**
     * Retorna o código de status HTTP.
     *
     * @return Código de status
     *
     * Returns the HTTP status code.
     *
     * @return Status code
     */
    public String getStatus() {
        return status;
    }

    /**
     * Retorna o tipo de erro HTTP.
     *
     * @return Tipo de erro
     *
     * Returns the HTTP error type.
     *
     * @return Error type
     */
    public String getError() {
        return error;
    }

    /**
     * Retorna a mensagem descritiva do erro.
     *
     * @return Mensagem de erro
     *
     * Returns the error description message.
     *
     * @return Error message
     */
    public String getMessage() {
        return message;
    }
}