package com.Iviinvest.controller;

import com.Iviinvest.dto.ErrorResponseDTO;
import com.Iviinvest.dto.ResetPasswordDTO;
import com.Iviinvest.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller responsável pelo gerenciamento de recuperação de senha.
 * <p>
 * Oferece endpoints para geração de tokens de recuperação e redefinição de senha.
 * <p>
 * Controller responsible for password recovery management.
 * Provides endpoints for recovery token generation and password reset.
 */
@RestController
@RequestMapping("/api/recover")
public class RecoveryController {

    private static final Logger log = LoggerFactory.getLogger(RecoveryController.class);

    private final UsuarioService service;

    /**
     * Construtor para injeção de dependência do serviço de usuário.
     *
     * @param service O serviço de usuário a ser injetado
     *
     * Constructor for dependency injection of the user service.
     *
     * @param service The user service to be injected
     */
    public RecoveryController(UsuarioService service) {
        this.service = service;
    }

    /**
     * Gera e envia um token para redefinição de senha para o e-mail fornecido.
     *
     * @param email E-mail do usuário para recuperação de senha (validado como formato de e-mail)
     * @return ResponseEntity com mensagem de sucesso ou erro
     *
     * Generates and sends a password reset token to the provided email.
     *
     * @param email User email for password recovery (validated as email format)
     * @return ResponseEntity with success or error message
     */
    @Operation(
            summary = "Gerar token para redefinir senha",
            description = "Gera um token único e envia por e-mail para permitir a redefinição de senha. "
                    + "Por segurança, sempre retorna sucesso mesmo para e-mails não cadastrados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Solicitação processada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\": \"200\", \"message\": \"Se este e-mail estiver cadastrado, enviaremos instruções para redefinir sua senha.\"}"
                            )
                    )
            ),
    })
    @GetMapping("/token/{email}")
    public ResponseEntity<?> generateToken(@PathVariable @Email String email) {

        String maskedEmail = email.replaceAll("(^.).*(@.*$)", "$1***$2");

        log.info("[GET] - Solicitação de token para: {}", maskedEmail);

        try {
            service.gerarTokenReset(email);
            log.info("[GET] - Token processado para: {}", maskedEmail);

            // Por segurança, sempre retorna mensagem positiva mesmo para e-mails não cadastrados
            // For security, always returns positive message even for unregistered emails
            return ResponseEntity.ok(Map.of(
                    "status", "200",
                    "message", "Se este e-mail estiver cadastrado, enviaremos instruções para redefinir sua senha."
            ));
        } catch (ResponseStatusException ex) {
            log.warn("[GET] - E-mail não encontrado (mas resposta mascarada): {}", maskedEmail);

            // Mantém a mesma resposta por questões de segurança
            // Keeps the same response for security reasons
            return ResponseEntity.ok(Map.of(
                    "status", "200",
                    "message", "Se este e-mail estiver cadastrado, enviaremos instruções para redefinir sua senha."
            ));
        } catch (Exception ex) {
            log.error("[GET] - Erro inesperado para: {} - Erro: {}", maskedEmail, ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "500",
                    "error", "INTERNAL_SERVER_ERROR",
                    "message", "Ocorreu um erro ao tentar enviar o e-mail de recuperação. Tente novamente mais tarde."
            ));
        }
    }

    /**
     * Redefine a senha do usuário usando um token válido.
     *
     * @param payload DTO contendo o token e a nova senha
     * @return ResponseEntity com mensagem de sucesso ou erro
     *
     * Resets user password using a valid token.
     *
     * @param payload DTO containing the token and new password
     * @return ResponseEntity with success or error message
     */
    @Operation(
            summary = "Redefinir senha com token",
            description = "Permite a alteração da senha mediante apresentação de um token válido de recuperação."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Senha redefinida com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Senha redefinida com sucesso\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Token inválido ou expirado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"status\": \"400\", \"error\": \"400 BAD_REQUEST\", \"message\": \"Token inválido\"}"
                            )
                    )
            )
    })
    @PutMapping("/password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDTO payload) {
        log.info("[PUT] - Solicitação de redefinição com token: {}",
                payload.getToken().substring(0, 4) + "..."); // Log parcial do token por segurança

        try {
            service.redefinirSenha(payload.getToken(), payload.getNewPassword());
            log.info("[PUT] - Senha alterada com sucesso para o token fornecido");
            return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso"));
        } catch (ResponseStatusException ex) {
            String motivo = ex.getReason() != null ? ex.getReason() : "Erro inesperado";

            if (ex.getStatusCode().value() == 400) {
                log.warn("[PUT] - Token inválido/expirado: {}",
                        payload.getToken().substring(0, 4) + "...");
            } else {
                log.error("[PUT] - Erro durante redefinição: {}", motivo);
            }

            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", motivo
            ));
        }
    }
}