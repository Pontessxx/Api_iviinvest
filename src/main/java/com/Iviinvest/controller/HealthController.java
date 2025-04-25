package com.Iviinvest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller de verificação de saúde da API.
 * <p>
 * Este endpoint é utilizado para validar se a aplicação está online e responsiva.
 * Ideal para health checks automatizados, como em ambientes de produção com load balancers.
 * <p>
 * Health check endpoint to verify if the application is running.
 * Useful for automated monitoring and deployment tools.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Retorna o status da API para verificação de disponibilidade.
     *
     * @return ResponseEntity com status "API online"
     *
     * Returns the health status of the API.
     *
     * @return ResponseEntity with status message
     */
    @Operation(
            summary = "Health check da API",
            description = "Verifica se a aplicação está rodando corretamente e responde com status 'API online'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "API disponível",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\": \"API online\"}")
                    )
            )
    })
    @GetMapping
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "API online"));
    }
}
