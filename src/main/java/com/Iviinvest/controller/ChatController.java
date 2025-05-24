package com.Iviinvest.controller;

import com.Iviinvest.dto.ChatRequestDTO;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.service.IAService;
import com.Iviinvest.service.UsuarioService;
import com.Iviinvest.util.EmailUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller responsável por lidar com o chat com IA.
 */
@RestController
@RequestMapping("/api/v1/carteiras/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final UsuarioService usuarioService;
    private final IAService iaService;

    public ChatController(UsuarioService usuarioService, IAService iaService) {
        this.usuarioService = usuarioService;
        this.iaService = iaService;
    }

    @Operation(
            summary = "Enviar pergunta para IA",
            description = "Gera uma resposta da IA com base na pergunta do usuário e nas informações do objetivo e da carteira.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resposta gerada com sucesso",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"resposta\": \"A carteira foi escolhida considerando seu perfil de investimento agressivo...\"}"
                            ))),
            @ApiResponse(responseCode = "500", description = "Erro interno",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\": \"500\", \"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erro ao processar a resposta da IA\"}"
                            )))
    })
    @PostMapping
    public ResponseEntity<?> conversarComIa(
            @AuthenticationPrincipal User userDetails,
            @RequestBody @Valid ChatRequestDTO pergunta) {

        String email = userDetails.getUsername();
        String masked = EmailUtils.mask(email);
        log.info("[CHAT] - Mensagem recebida de {}: {}", masked, pergunta.getQuestion());


        try {
            Usuario usuario = usuarioService.findByEmail(email);

            JSONObject respostaJson = iaService.responderPergunta(pergunta.getQuestion(), usuario);

            return ResponseEntity.ok(Map.of("resposta", respostaJson.getString("resposta")));

        } catch (Exception e) {
            log.error("[CHAT] - Erro ao gerar resposta da IA para {}: {}", masked, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "500",
                    "error", "INTERNAL_SERVER_ERROR",
                    "message", "Erro ao processar a resposta da IA"
            ));
        }
    }
}
