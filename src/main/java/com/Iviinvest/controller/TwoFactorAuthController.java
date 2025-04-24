// controller/TwoFactorAuthController.java
package com.Iviinvest.controller;

import com.Iviinvest.dto.TwoFactorCodeDTO;
import com.Iviinvest.dto.TwoFactorRequestDTO;
import com.Iviinvest.service.JwtService;
import com.Iviinvest.service.TwoFactorService;
import com.Iviinvest.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/2fa")
public class TwoFactorAuthController {

    private static final Logger log = LoggerFactory.getLogger(TwoFactorAuthController.class);

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/send")
    public ResponseEntity<?> sendToken(@RequestBody TwoFactorCodeDTO dto) {
        twoFactorService.generateAndSendToken(dto.getEmail());
        String maskedEmail = dto.getEmail().replaceAll("(^.).*(@.*$)", "$1***$2");
        log.info("[2FA SEND] Código gerado para: {}", maskedEmail);
        return ResponseEntity.ok("Código enviado com sucesso.");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody TwoFactorRequestDTO dto) {
        boolean valid = twoFactorService.verifyToken(dto.getEmail(), dto.getToken());
        if (!valid) {
            return ResponseEntity.status(401).body("Código inválido ou expirado");
        }

        var usuario = usuarioService.findByEmail(dto.getEmail());
        String jwt = jwtService.generateToken(usuario);
        return ResponseEntity.ok(Map.of("token", jwt));
    }
}
