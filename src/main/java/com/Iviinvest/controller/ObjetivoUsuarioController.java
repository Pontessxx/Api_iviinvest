package com.Iviinvest.controller;

import com.Iviinvest.dto.ObjetivoUsuarioDTO;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.service.ObjetivoUsuarioService;
import com.Iviinvest.service.UsuarioService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Controller responsável pelo gerenciamento de objetivos de investimento do usuário.
 */
@RestController
@RequestMapping("/api/objetivos")
public class ObjetivoUsuarioController {

    private static final Logger log = LoggerFactory.getLogger(ObjetivoUsuarioController.class);

    private final ObjetivoUsuarioService objetivoService;
    private final UsuarioService usuarioService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ObjetivoUsuarioController(ObjetivoUsuarioService objetivoService, UsuarioService usuarioService) {
        this.objetivoService = objetivoService;
        this.usuarioService = usuarioService;
    }

    /**
     * Salva ou atualiza os dados de objetivo de investimento do usuário autenticado.
     *
     * @param userDetails Dados do usuário autenticado
     * @param dto Dados do objetivo de investimento
     * @return Mensagem de sucesso ou erro
     */
    @Operation(summary = "Salvar dados de objetivo do usuário",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Objetivo salvo com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Objetivo salvo com sucesso\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\": \"400\", \"error\": \"BAD_REQUEST\", \"message\": \"Erro de validação\"}"
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> salvarObjetivo(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails,
            @RequestBody @Valid ObjetivoUsuarioDTO dto) {
        String email = userDetails.getUsername();
        log.info("[POST] - Solicitado salvamento de objetivo para usuário: {}", email);

        try {
            Usuario usuario = usuarioService.findByEmail(email);
            objetivoService.salvarObjetivo(usuario, dto);

            log.info("[POST] - Objetivo salvo com sucesso para usuário: {}", email);
            return ResponseEntity.ok(Map.of("message", "Objetivo salvo com sucesso"));

        } catch (ResponseStatusException ex) {
            log.error("[POST] - Falha ao salvar objetivo para usuário {}: {}", email, ex.getReason());
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
            ));
        } catch (Exception ex) {
            log.error("[POST] - Erro interno ao salvar objetivo para usuário {}: {}", email, ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "500",
                    "error", "INTERNAL_SERVER_ERROR",
                    "message", "Erro inesperado ao salvar objetivo."
            ));
        }
    }

    /**
     * Recupera os dados de objetivo de investimento do usuário autenticado.
     *
     * @param userDetails Dados do usuário autenticado
     * @return Objetivo de investimento ou mensagem de erro
     */
    @Operation(summary = "Buscar último objetivo do usuário",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Último objetivo encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"objetivo\": \"Aposentadoria\",\n" +
                                            "  \"prazo\": \"15+ anos\",\n" +
                                            "  \"valorInicial\": 20000.0,\n" +
                                            "  \"aporteMensal\": 500.0,\n" +
                                            "  \"patrimonioAtual\": 50000.0,\n" +
                                            "  \"liquidez\": \"Baixa\",\n" +
                                            "  \"setoresEvitar\": [\"Criptomoedas\", \"Fundos de Investimentos\"]\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhum objetivo encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\": \"404\", \"error\": \"NOT_FOUND\", \"message\": \"Nenhum objetivo encontrado para o usuário.\"}"
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<?> buscarUltimoObjetivo(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        String email = userDetails.getUsername();
        log.info("[GET] - Solicitada busca do último objetivo para usuário: {}", email);

        try {
            Usuario usuario = usuarioService.findByEmail(email);

            return objetivoService.buscarUltimoPorUsuario(usuario)
                    .map(objetivo -> {
                        try {
                            List<String> setoresEvitar = objectMapper.readValue(objetivo.getSetoresEvitar(), new TypeReference<>() {});
                            ObjetivoUsuarioDTO dto = new ObjetivoUsuarioDTO();
                            dto.setObjetivo(objetivo.getObjetivo());
                            dto.setPrazo(objetivo.getPrazo());
                            dto.setValorInicial(objetivo.getValorInicial());
                            dto.setAporteMensal(objetivo.getAporteMensal());
                            dto.setPatrimonioAtual(objetivo.getPatrimonioAtual());
                            dto.setLiquidez(objetivo.getLiquidez());
                            dto.setSetoresEvitar(setoresEvitar);

                            log.info("[GET] - Último objetivo encontrado e retornado para usuário: {}", email);
                            return ResponseEntity.ok(dto);

                        } catch (Exception ex) {
                            log.error("[GET] - Erro ao ler setores evitados para usuário {}: {}", email, ex.getMessage(), ex);
                            return ResponseEntity.internalServerError().body(Map.of(
                                    "status", "500",
                                    "error", "INTERNAL_SERVER_ERROR",
                                    "message", "Erro ao processar setores evitados."
                            ));
                        }
                    })
                    .orElseGet(() -> {
                        log.warn("[GET] - Nenhum objetivo encontrado para usuário: {}", email);
                        return ResponseEntity.status(404).body(Map.of(
                                "status", "404",
                                "error", "NOT_FOUND",
                                "message", "Nenhum objetivo encontrado para o usuário."
                        ));
                    });

        } catch (ResponseStatusException ex) {
            log.error("[GET] - Falha ao buscar último objetivo para usuário {}: {}", email, ex.getReason());
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
            ));
        } catch (Exception ex) {
            log.error("[GET] - Erro interno ao buscar último objetivo para usuário {}: {}", email, ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "500",
                    "error", "INTERNAL_SERVER_ERROR",
                    "message", "Erro inesperado ao buscar último objetivo."
            ));
        }
    }

    /**
     * Lista o histórico completo de objetivos de investimento do usuário.
     *
     * @param userDetails Dados do usuário autenticado
     * @return Lista de Objetivos de investimento
     */
    @Operation(summary = "Listar histórico completo de objetivos do usuário",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Histórico encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "[{\"objetivo\":\"Aposentadoria\",\"prazo\":\"15+ anos\",\"valorInicial\":20000.0,\"aporteMensal\":500.0,\"patrimonioAtual\":50000.0,\"liquidez\":\"Baixa\",\"setoresEvitar\":[\"Criptomoedas\"]}]"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhum objetivo encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\": \"404\", \"error\": \"NOT_FOUND\", \"message\": \"Nenhum objetivo encontrado para o usuário.\"}"
                            )
                    )
            )
    })
    @GetMapping("/historico")
    public ResponseEntity<?> listarHistoricoObjetivos(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        String email = userDetails.getUsername();
        log.info("[GET] - Solicitada listagem do histórico de objetivos para usuário: {}", email);

        try {
            Usuario usuario = usuarioService.findByEmail(email);
            List<ObjetivoUsuarioDTO> objetivos = objetivoService.buscarHistoricoPorUsuario(usuario);

            if (objetivos.isEmpty()) {
                log.warn("[GET] - Nenhum objetivo encontrado para usuário: {}", email);
                return ResponseEntity.status(404).body(Map.of(
                        "status", "404",
                        "error", "NOT_FOUND",
                        "message", "Nenhum objetivo encontrado para o usuário."
                ));
            }

            log.info("[GET] - {} objetivos retornados para histórico do usuário: {}", objetivos.size(), email);
            return ResponseEntity.ok(objetivos);

        } catch (ResponseStatusException ex) {
            log.error("[GET] - Falha ao buscar histórico para usuário {}: {}", email, ex.getReason());
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
            ));
        } catch (Exception ex) {
            log.error("[GET] - Erro interno ao buscar histórico para usuário {}: {}", email, ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "500",
                    "error", "INTERNAL_SERVER_ERROR",
                    "message", "Erro inesperado ao buscar histórico de objetivos."
            ));
        }
    }

}
