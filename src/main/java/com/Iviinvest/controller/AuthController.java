package com.Iviinvest.controller;

import com.Iviinvest.dto.ErrorResponseDTO;
import com.Iviinvest.dto.LoginDTO;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.repository.UsuarioRepository;
import com.Iviinvest.service.JwtService;
import com.Iviinvest.service.UsuarioService;
import com.Iviinvest.util.EmailUtils;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Controller responsável por lidar com operações de autenticação.
 * <p>
 * Controller responsible for handling authentication operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioService service;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    /**
     * Construtor para injeção de dependência do serviço de usuário.
     *
     * @param service O serviço de usuário a ser injetado
     *
     * Constructor for dependency injection of the user service.
     *
     * @param service The user service to be injected
     */
    public AuthController(UsuarioService service, UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }


    /**
     * Realiza o login do usuário e retorna um token JWT em caso de sucesso.
     * <p>
     * Endpoint: POST /api/auth/login
     *
     * @param dto Objeto de transferência de dados contendo credenciais de login (email e senha)
     * @return ResponseEntity contendo o token JWT ou mensagem de erro
     * @throws ResponseStatusException em caso de credenciais inválidas ou usuário não encontrado
     *
     * Authenticates user credentials and returns a JWT token upon success.
     *
     * @param dto Data transfer object containing login credentials (email and password)
     * @return ResponseEntity containing JWT token or error message
     * @throws ResponseStatusException for invalid credentials or user not found
     */
    @Operation(
            summary = "Login do usuário",
            description = "Autentica as credenciais do usuário e retorna um token JWT válido para autorização."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"email\": \"usuario@exemplo.com\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Senha incorreta",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"status\": \"401\", \"error\": \"401 UNAUTHORIZED\", \"message\": \"Senha incorreta\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"status\": \"404\", \"error\": \"404 NOT_FOUND\", \"message\": \"Usuário não encontrado\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno no servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"status\": \"500\", \"error\": \"500 INTERNAL_SERVER_ERROR\", \"message\": \"Erro inesperado\"}"
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO dto) {

        String maskedEmail = EmailUtils.mask(dto.getEmail());

        log.info("---------- [POST] - LOGIN ATTEMPT FOR: {} ----------", maskedEmail);

        try {
            // Autentica o usuário usando o serviço e obtém o token JWT
            // Authenticates user using service and gets JWT token
            String token = service.autenticar(dto);

            log.info("SUCCESSFUL LOGIN FOR USER: {}", maskedEmail);
            // Retorna token + email no response
            return ResponseEntity.ok(Map.of(
                    "email", dto.getEmail()
            ));

        } catch (ResponseStatusException ex) {
            // Tratamento de erros específicos com códigos de status conhecidos
            // Handling specific errors with known status codes
            String errorMessage = ex.getReason() != null ? ex.getReason() : "Erro inesperado";

            // Log apropriado baseado no tipo de erro
            // Appropriate logging based on error type
            if (ex.getStatusCode().value() == 401) {
                log.warn("INVALID PASSWORD ATTEMPT FOR EMAIL: {}", maskedEmail);
            } else if (ex.getStatusCode().value() == 404) {
                log.warn("LOGIN ATTEMPT WITH UNREGISTERED EMAIL: {}", maskedEmail);
            } else {
                log.error("UNHANDLED LOGIN ERROR: status={}, email={}, reason={}",
                        ex.getStatusCode().value(), maskedEmail, errorMessage);
            }

            // Retorna resposta de erro estruturada
            // Returns structured error response
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Map.of(
                            "status", String.valueOf(ex.getStatusCode().value()),
                            "error", ex.getStatusCode().toString(),
                            "message", errorMessage
                    ));
        } catch (Exception ex) {
            // Tratamento genérico para erros não esperados
            // Generic handling for unexpected errors
            log.error("INTERNAL SERVER ERROR DURING LOGIN: email={}, error={}",
                    dto.getEmail(), ex.getMessage(), ex);

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "status", "500",
                            "error", "INTERNAL_SERVER_ERROR",
                            "message", "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde."
                    ));
        }
    }



    @PostMapping("/login-face")
    public ResponseEntity<?> loginComFace(
            @RequestHeader(value = "X-Internal-Key", required = false) String keyHeader,
            @RequestBody Map<String, String> body,
            HttpServletRequest req) {

        // 🔎 Log de todos os headers (debug)
        //var names = req.getHeaderNames();
        //while (names.hasMoreElements()) {
        //    String n = names.nextElement();
        //    log.info("[HDR] {} = {}", n, req.getHeader(n));
        //}

        // 🔄 Tenta também a variação em minúsculas (alguns proxies convertem)
        String alt = req.getHeader("x-internal-key");

        // 🧼 Normaliza
        String key = keyHeader != null ? keyHeader.trim() : (alt != null ? alt.trim() : null);

        // ⚠️ Log do valor recebido e tamanho (pega caracteres invisíveis)
        //log.info("[FACE-LOGIN] X-Internal-Key recebido='{}' len={}", key, (key == null ? 0 : key.length()));

        // ✅ Compare com trim
        final String EXPECTED = "MINHA_CHAVE_INTERNA_SECRETA";
        if (key == null || !EXPECTED.equals(key)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chave interna inválida");
        }

        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail é obrigatório");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));

        String token = jwtService.generateToken(usuario);
        return ResponseEntity.ok(Map.of("token", token));
    }



}