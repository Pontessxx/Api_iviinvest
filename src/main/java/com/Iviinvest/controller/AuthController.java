package com.Iviinvest.controller;

import com.Iviinvest.dto.ErrorResponseDTO;
import com.Iviinvest.dto.LoginDTO;
import com.Iviinvest.service.UsuarioService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Login do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Login realizado com sucesso\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Senha incorreta",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\": \"401\", \"error\": \"401 UNAUTHORIZED\", \"message\": \"Senha incorreta\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\": \"404\", \"error\": \"404 NOT_FOUND\", \"message\": \"Usuário não encontrado\"}")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\": \"500\", \"error\": \"500 INTERNAL_SERVER_ERROR\", \"message\": \"Erro inesperado\"}")
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO dto) {
        log.info("---------- [LOG] - LOGIN ----------");
        try {
            // verifica pelo dto o login { email: "", senha: "" }
            service.autenticar(dto);
            log.info("login sucedido para : {}", dto.getEmail());
            return ResponseEntity.ok(Map.of(
                    "message", "Login realizado com sucesso"
            ));
        } catch (ResponseStatusException ex) {
            String errorMessage = ex.getReason() != null ? ex.getReason() : "Erro inesperado";

            if (ex.getStatusCode().value() == 401) {
                log.warn("Senha incorreta para email: {}", dto.getEmail());
            } else if (ex.getStatusCode().value() == 404) {
                log.warn("Tentativa de login com email não cadastrado: {}", dto.getEmail());
            } else {
                log.error("Erro não tratado durante login: status={}, email={}, motivo={}",
                        ex.getStatusCode().value(), dto.getEmail(), errorMessage);
            }

            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Map.of(
                            "status", String.valueOf(ex.getStatusCode().value()),
                            "error", ex.getStatusCode().toString(),
                            "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
                    ));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(500)
                    .body(Map.of(
                            "status", "500",
                            "error", "INTERNAL_SERVER_ERROR",
                            "message", ex.getMessage()
                    ));
        }
    }

}
