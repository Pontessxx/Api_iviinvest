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

@RestController
@RequestMapping("/api/recover")
public class RecoveryController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioService service;

    public RecoveryController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Gerar token para redefinir senha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token gerado com sucesso",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"token\": \"123e4567-e89b-12d3-a456-426614174000\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\": \"404\", \"error\": \"404 NOT_FOUND\", \"message\": \"Usuário não encontrado\"}")
                    )
            )
    })
    @GetMapping("/token/{email}")
    public ResponseEntity<?> generateToken(@PathVariable @Email String email) {
        log.info("Requisição recebida para geração de token de recuperação - Email: {}", email);

        try {
            service.gerarTokenReset(email);
            log.info("Processo de geração de token concluído.");
        } catch (ResponseStatusException ex) {
            log.warn("Tentativa com e-mail inexistente: {}", email);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Se este e-mail estiver cadastrado, enviaremos instruções para redefinir sua senha."
        ));
    }

    @Operation(summary = "Redefinir senha com token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "\"Password reset successfully.\"")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\": \"400\", \"error\": \"400 BAD_REQUEST\", \"message\": \"Token inválido\"}")
                    )
            )
    })
    @PutMapping("/password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDTO payload) {
        log.info("Requisição recebida para redefinição de senha com token: {}", payload.getToken());

        try {
            service.redefinirSenha(payload.getToken(), payload.getNewPassword());
            log.info("Senha redefinida com sucesso para token informado.");
            return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso"));
        } catch (ResponseStatusException ex) {
            String motivo = ex.getReason() != null ? ex.getReason() : "Erro inesperado";

            if (ex.getStatusCode().value() == 400) {
                log.warn("Token inválido ou expirado: {}", payload.getToken());
            } else {
                log.error("Erro ao redefinir senha: {}", motivo);
            }

            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
            ));
        }
    }
}
