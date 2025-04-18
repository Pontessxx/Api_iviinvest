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

@RestController
@RequestMapping("/api/recover")
public class RecoveryController {

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
        try {
            String token = service.gerarTokenReset(email);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (ResponseStatusException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Map.of(
                            "status", String.valueOf(ex.getStatusCode().value()),
                            "error", ex.getStatusCode().toString(),
                            "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
                    ));
        }
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
        try {
            service.redefinirSenha(payload.getToken(), payload.getNewPassword());
            return ResponseEntity.ok("Password reset successfully.");
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
            ));
        }
    }
}
