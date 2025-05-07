package com.Iviinvest.controller;

import com.Iviinvest.dto.ChatRequestDTO;
import com.Iviinvest.model.CarteiraUsuario;
import com.Iviinvest.model.ObjetivoUsuario;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.service.CarteiraUsuarioService;
import com.Iviinvest.service.IAService;
import com.Iviinvest.service.ObjetivoUsuarioService;
import com.Iviinvest.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/carteiras/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final UsuarioService         usuarioService;
    private final ObjetivoUsuarioService objetivoService;
    private final CarteiraUsuarioService carteiraUsuarioService;
    private final IAService              iaService;

    @Autowired
    public ChatController(UsuarioService usuarioService,
                          ObjetivoUsuarioService objetivoService,
                          CarteiraUsuarioService carteiraUsuarioService,
                          IAService iaService) {
        this.usuarioService        = usuarioService;
        this.objetivoService       = objetivoService;
        this.carteiraUsuarioService= carteiraUsuarioService;
        this.iaService             = iaService;
    }

    @PostMapping
    @Operation(
            summary = "Chat explicativo (apenas texto)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> explainOnly(
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody ChatRequestDTO request
    ) {
        // 1) buscar usuário e objetivo
        Usuario u = usuarioService.findByEmail(userDetails.getUsername());
        ObjetivoUsuario obj = objetivoService
                .buscarUltimoPorUsuario(u)
                .orElseThrow(() -> new RuntimeException("Nenhum objetivo cadastrado"));
        CarteiraUsuario cu = carteiraUsuarioService
                .buscarPorObjetivo(obj)
                .orElseThrow(() -> new RuntimeException("Nenhuma carteira confirmada"));

        // 2) montar prompt
        String prompt = iaService.montarPromptSimples(obj, cu, request.getQuestion());

        try {
            // 3) chamar IA
            JSONObject iaResp = iaService.chamarOpenAI(prompt);
            String explanation = iaResp.optString("explanation", iaResp.toString());

            // 4) retornar apenas o texto explicativo
            return ResponseEntity.ok(Map.of("explanation", explanation));

        } catch (Exception e) {
            log.error("Erro ao chamar OpenAI: ", e);
            // 5) em caso de falha, retorna 500 com mensagem de erro
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Falha no serviço de IA: " + e.getMessage()));
        }
    }
}
