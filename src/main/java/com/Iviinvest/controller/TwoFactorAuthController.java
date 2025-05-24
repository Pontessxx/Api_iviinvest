package com.Iviinvest.controller;

import com.Iviinvest.dto.TwoFactorCodeDTO;
import com.Iviinvest.dto.TwoFactorRequestDTO;
import com.Iviinvest.service.JwtService;
import com.Iviinvest.service.TwoFactorService;
import com.Iviinvest.service.UsuarioService;
import com.Iviinvest.util.EmailUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller responsável pelo gerenciamento de autenticação em dois fatores (2FA).
 * <p>
 * Fornece endpoints para envio e verificação de tokens de autenticação.
 * <p>
 * Handles two-factor authentication (2FA) token generation and verification.
 */
@RestController
@RequestMapping("/api/v1/auth/2fa")
public class TwoFactorAuthController {

    private static final Logger log = LoggerFactory.getLogger(TwoFactorAuthController.class);

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Envia um token de autenticação 2FA para o e-mail do usuário.
     *
     * @param dto Objeto contendo o e-mail do usuário
     * @return ResponseEntity com mensagem de confirmação
     *
     * Sends a 2FA code to the user's email.
     *
     * @param dto DTO containing the user's email
     * @return ResponseEntity with confirmation message
     */
    @Operation(
            summary = "Enviar código de autenticação 2FA",
            description = "Gera e envia um código de autenticação em dois fatores para o e-mail do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Código enviado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "\"Código enviado com sucesso.\"")
                    )
            )
    })
    @PostMapping("/send")
    public ResponseEntity<String> sendToken(@RequestBody TwoFactorCodeDTO dto) {
        twoFactorService.generateAndSendToken(dto.getEmail());

        String maskedEmail = EmailUtils.mask(dto.getEmail());
        log.info("[2FA SEND] Código gerado para: {}", maskedEmail);

        return ResponseEntity.ok("Código enviado com sucesso.");
    }

    /**
     * Verifica o token de autenticação 2FA informado pelo usuário.
     *
     * @param dto Objeto contendo o e-mail e o token recebido
     * @return ResponseEntity contendo o token JWT gerado, ou erro caso inválido
     *
     * Verifies the 2FA token submitted by the user.
     *
     * @param dto DTO containing the email and received token
     * @return ResponseEntity with JWT token or error message
     */
    @Operation(
            summary = "Verificar código de autenticação 2FA",
            description = "Valida o código 2FA enviado ao usuário e retorna um token JWT caso o código seja válido."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token válido. Retorna JWT de autenticação",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI...\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Código inválido ou expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "\"Código inválido ou expirado\"")
                    )
            )
    })
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody TwoFactorRequestDTO dto) {
        boolean valid = twoFactorService.verifyToken(dto.getEmail(), dto.getToken());
        String maskedEmail = EmailUtils.mask(dto.getEmail());

        if (!valid) {
            log.warn("[2FA VERIFY] Código inválido ou expirado para: {}", maskedEmail);
            return ResponseEntity.status(401).body("Código inválido ou expirado");
        }

        var usuario = usuarioService.findByEmail(dto.getEmail());
        String jwt = jwtService.generateToken(usuario);

        log.info("[2FA VERIFY] Código validado com sucesso para: {}", maskedEmail);
        return ResponseEntity.ok(Map.of("token", jwt));
    }
}
