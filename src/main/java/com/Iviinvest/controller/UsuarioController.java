package com.Iviinvest.controller;

import com.Iviinvest.dto.*;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller responsável por operações relacionadas a usuários.
 * <p>
 * Oferece endpoints para registro, listagem, atualização e exclusão de usuários,
 * além de gerenciamento de perfis de investidores.
 * <p>
 * Controller responsible for user-related operations.
 * Provides endpoints for user registration, listing, updating, and deletion,
 * as well as investor profile management.
 */
@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService service;

    /**
     * Construtor para injeção de dependência do serviço de usuário.
     *
     * @param service O serviço de usuário a ser injetado
     *
     * Constructor for dependency injection of the user service.
     *
     * @param service The user service to be injected
     */
    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    /**
     * Registra um novo usuário no sistema.
     *
     * @param dto DTO contendo informações de registro do usuário
     * @return ResponseEntity com o usuário criado
     *
     * Registers a new user in the system.
     *
     * @param dto DTO containing user registration information
     * @return ResponseEntity with the created user
     */
    @Operation(
            summary = "Cadastrar novo usuário",
            description = "Cria uma nova conta de usuário com os dados fornecidos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário cadastrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"id\": 1, \"email\": \"usuario@email.com\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"status\": \"500\", \"error\": \"500 INTERNAL_SERVER_ERROR\", \"message\": \"Erro inesperado\"}"
                            )
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<?> cadastrar(@RequestBody @Valid UserRegisterDTO dto) {
        log.info("[POST] - Tentativa de cadastro para email: {}", dto.getEmail());

        try {
            // 1. Cadastra o usuário
            Usuario usuario = service.cadastrarUsuario(dto);

            // 2. Gera o token JWT automaticamente após o cadastro
            String token = service.autenticar(new LoginDTO(dto.getEmail(), dto.getSenha()));

            log.info("[POST] - Usuário cadastrado com ID: {} e email: {}", usuario.getId(), usuario.getEmail());

            // 3. Retorna os dados do usuário + token
            return ResponseEntity.ok(Map.of(
                    "id", usuario.getId(),
                    "email", usuario.getEmail(),
                    "token", token
            ));

        } catch (ResponseStatusException ex) {
            log.error("[POST] - Erro ao cadastrar usuário: {}", ex.getReason());
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", ex.getStatusCode().value(),
                    "error",  ex.getStatusCode().toString(),
                    "message", ex.getReason()
            ));
        }
    }

    /**
     * Retorna uma lista de todos os usuários (informações públicas).
     *
     * @return ResponseEntity contendo lista de DTOs públicos de usuários
     *
     * Returns a list of all users (public information).
     *
     * @return ResponseEntity containing list of public user DTOs
     */
    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna uma lista com informações públicas de todos os usuários cadastrados.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso"
            )
    })
    @GetMapping
    public ResponseEntity<List<UsuarioPublicDTO>> listarTodos() {
        log.info("[GET] - Solicitada listagem de todos os usuários");
        List<UsuarioPublicDTO> usuarios = service.listarTodosUsuariosPublicos();
        log.info("[GET] - Retornados {} usuários", usuarios.size());
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Busca um usuário específico pelo ID.
     *
     * @param id ID do usuário a ser buscado
     * @return ResponseEntity com informações públicas do usuário ou mensagem de erro
     *
     * Finds a specific user by ID.
     *
     * @param id ID of the user to be found
     * @return ResponseEntity with public user information or error message
     */
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Recupera informações públicas de um usuário específico baseado no ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"id\": 1, \"email\": \"usuario@email.com\"}"
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
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        log.info("[GET] - Buscando usuário com ID: {}", id);
        try {
            UsuarioPublicDTO usuario = service.buscarUsuarioPublicoPorId(id);
            log.info("[GET] - Usuário encontrado: ID {}", id);
            return ResponseEntity.ok(usuario);
        } catch (ResponseStatusException ex) {
            log.warn("[GET] - Usuário não encontrado: ID {}, Erro: {}", id, ex.getReason());
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
            ));
        }
    }

    /**
     * Atualiza as informações de um usuário existente.
     *
     * @param id ID do usuário a ser atualizado
     * @param dto DTO contendo novas informações do usuário
     * @return ResponseEntity com o usuário atualizado ou mensagem de erro
     *
     * Updates information of an existing user.
     *
     * @param id ID of the user to be updated
     * @param dto DTO containing new user information
     * @return ResponseEntity with the updated user or error message
     */
    @Operation(
            summary = "Atualizar usuário por ID",
            description = "Atualiza as informações de um usuário existente baseado no ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"id\": 1, \"email\": \"novo@email.com\"}"
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
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody @Valid UserRegisterDTO dto) {
        log.info("[PUT] - Iniciando atualização para usuário ID: {}", id);
        try {
            Usuario atualizado = service.atualizar(id, dto);
            log.info("[PUT] - Usuário atualizado com sucesso: ID {}", id);
            return ResponseEntity.ok(atualizado);
        } catch (ResponseStatusException ex) {
            log.error("[PUT] - Falha ao atualizar usuário ID {}: {}", id, ex.getReason());
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
            ));
        }
    }

    /**
     * Remove um usuário do sistema.
     *
     * @param usuario token a ser removido
     * @return ResponseEntity vazio (204) em caso de sucesso
     *
     * Removes a user from the system.
     *
     * @param usuario token of the user to be removed
     * @return Empty ResponseEntity (204) on success
     */
    @Operation(
            summary = "Excluir usuário por Token",
            description = "Remove permanentemente um usuário do sistema baseado no token autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuário deletado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"status\": \"500\", \"error\": \"500 INTERNAL_SERVER_ERROR\", \"message\": \"Erro inesperado\"}"
                            )
                    )
            )
    })
    @DeleteMapping
    public ResponseEntity<Void> deletarConta(@AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        String email = userDetails.getUsername(); // pega o email do token
        log.info("[DELETE] - Solicitada exclusão para usuário: {}", email);

        Usuario usuario = service.findByEmail(email);
        service.deletar(usuario.getId());

        log.info("[DELETE] - Conta do usuário excluída: {}", email);
        return ResponseEntity.noContent().build();
    }

    /**
     * Recupera o perfil de investidor do usuário autenticado.
     *
     * @param userDetails Detalhes do usuário autenticado
     * @return ResponseEntity com o perfil de investidor
     *
     * Retrieves the investor profile of the authenticated user.
     *
     * @param userDetails Authenticated user details
     * @return ResponseEntity with the investor profile
     */
    @Operation(
            summary = "Buscar perfil de investidor",
            description = "Recupera o perfil de investidor do usuário autenticado.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil de investidor retornado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"perfilInvestidor\": \"Agressivo\"}"
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
            )
    })
    @GetMapping("/perfil")
    public ResponseEntity<PerfilDTO> buscarPerfil(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        String email = userDetails.getUsername();
        log.info("[GET] - Buscando perfil para usuário: {}", email);
        String perfil = service.buscarPerfilInvestidorPorEmail(email);
        log.info("[GET] - Perfil encontrado: {} para usuário: {}", perfil, email);
        return ResponseEntity.ok(new PerfilDTO(perfil));
    }

    /**
     * Atualiza o perfil de investidor do usuário autenticado.
     *
     * @param userDetails Detalhes do usuário autenticado
     * @param dto DTO contendo o novo perfil de investidor
     * @return ResponseEntity com o perfil atualizado
     *
     * Updates the investor profile of the authenticated user.
     *
     * @param userDetails Authenticated user details
     * @param dto DTO containing the new investor profile
     * @return ResponseEntity with the updated profile
     */
    @Operation(
            summary = "Atualizar perfil de investidor",
            description = "Atualiza o perfil de investidor do usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"perfilInvestidor\": \"Moderado\"}"
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
            )
    })
    @PutMapping("/perfil")
    public ResponseEntity<PerfilDTO> atualizarPerfil(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails,
            @RequestBody PerfilDTO dto) {
        String email = userDetails.getUsername();
        log.info("[PROFILE-UPDATE] - Atualizando perfil para usuário: {}", email);
        Usuario atualizado = service.atualizarPerfilInvestidorPorEmail(email, dto.getPerfilInvestidor());
        log.info("[PROFILE-UPDATE] - Perfil atualizado para: {} do usuário: {}",
                dto.getPerfilInvestidor(), email);
        return ResponseEntity.ok(new PerfilDTO(atualizado.getPerfilInvestidor()));
    }
}