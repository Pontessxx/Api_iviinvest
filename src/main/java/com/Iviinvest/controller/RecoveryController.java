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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Controller responsável pelo gerenciamento de recuperação de senha.
 */
@RestController
@RequestMapping("/api/v1/recover")
public class RecoveryController {

    private static final Logger log = LoggerFactory.getLogger(RecoveryController.class);

    private final UsuarioService service;

    public RecoveryController(UsuarioService service) {
        this.service = service;
    }

    /**
     * Gera e envia um token de redefinição de senha para o e-mail fornecido.
     *
     * @param request Mapa contendo o campo "email"
     * @return ResponseEntity com mensagem de sucesso
     */
    @Operation(
            summary = "Solicitar envio de token para redefinição de senha",
            description = "Recebe um e-mail e, se cadastrado, envia um token para redefinir a senha. Sempre retorna sucesso por segurança."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Solicitação processada (token enviado, se o e-mail existir)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\": \"200\", \"message\": \"Se este e-mail estiver cadastrado, enviaremos instruções para redefinir sua senha.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno ao tentar gerar o token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\": \"500\", \"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Ocorreu um erro ao tentar enviar o e-mail de recuperação.\"}"
                            )
                    )
            )
    })
    @PostMapping("/token")
    public ResponseEntity<?> generateToken(@RequestBody Map<String, @Email String> request) {
        String email = request.get("email");
        String maskedEmail = email.replaceAll("(^.).*(@.*$)", "$1***$2");

        log.info("[POST] - Solicitação de token para redefinição de senha para: {}", maskedEmail);

        try {
            service.gerarTokenReset(email);
            log.info("[POST] - Token processado para: {}", maskedEmail);

            return ResponseEntity.ok(Map.of(
                    "status", "200",
                    "message", "Se este e-mail estiver cadastrado, enviaremos instruções para redefinir sua senha."
            ));

        } catch (ResponseStatusException ex) {
            log.warn("[POST] - Erro lógico durante envio de token (mas resposta mascarada): {}", maskedEmail);

            // Sempre retorna 200 para não revelar existência do email
            return ResponseEntity.ok(Map.of(
                    "status", "200",
                    "message", "Se este e-mail estiver cadastrado, enviaremos instruções para redefinir sua senha."
            ));
        } catch (Exception ex) {
            log.error("[POST] - Erro inesperado para {}: {}", maskedEmail, ex.getMessage(), ex);
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
     */
    @Operation(
            summary = "Redefinir senha usando token",
            description = "Permite redefinir a senha informando o token recebido no e-mail."
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
                            examples = @ExampleObject(
                                    value = "{\"status\": \"400\", \"error\": \"BAD_REQUEST\", \"message\": \"Token inválido\"}"
                            )
                    )
            )
    })
    @PutMapping("/password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDTO payload) {
        log.info("[PUT] - Solicitação de redefinição de senha com token: {}...", payload.getToken().substring(0, 4));

        try {
            service.redefinirSenha(payload.getToken(), payload.getNewPassword());
            log.info("[PUT] - Senha alterada com sucesso para o token informado");
            return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso"));

        } catch (ResponseStatusException ex) {
            String motivo = ex.getReason() != null ? ex.getReason() : "Erro inesperado";

            if (ex.getStatusCode().value() == 400) {
                log.warn("[PUT] - Token inválido ou expirado: {}...", payload.getToken().substring(0, 4));
            } else {
                log.error("[PUT] - Erro ao redefinir senha: {}", motivo);
            }

            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", motivo
            ));
        }
    }
}
