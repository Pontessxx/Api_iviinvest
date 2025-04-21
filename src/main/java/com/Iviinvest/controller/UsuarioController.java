package com.Iviinvest.controller;

import com.Iviinvest.dto.ErrorResponseDTO;
import com.Iviinvest.dto.PerfilDTO;
import com.Iviinvest.dto.UserRegisterDTO;
import com.Iviinvest.dto.UsuarioPublicDTO;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);


    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastrar novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"id\": 1, \"email\": \"usuario@email.com\"}")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Erro interno",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\": \"500\", \"error\": \"500 INTERNAL_SERVER_ERROR\", \"message\": \"Erro inesperado\"}")
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<Usuario> cadastrar(@RequestBody @Valid UserRegisterDTO dto) {
        log.info("Requisição para cadastro de novo usuário: {}", dto.getEmail());
        Usuario usuario = service.cadastrarUsuario(dto);
        log.info("Usuário cadastrado com sucesso: id={}, email={}", usuario.getId(), usuario.getEmail());
        return ResponseEntity.ok(usuario);
    }

    @Operation(summary = "Listar todos os usuários")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<UsuarioPublicDTO>> listarTodos() {
        List<UsuarioPublicDTO> usuarios = service.listarTodosUsuariosPublicos();
        return ResponseEntity.ok(usuarios);
    }


    @Operation(summary = "Buscar usuário por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"id\": 1, \"email\": \"usuario@email.com\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\": \"404\", \"error\": \"404 NOT_FOUND\", \"message\": \"Usuário não encontrado\"}")
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            UsuarioPublicDTO usuario = service.buscarUsuarioPublicoPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
            ));
        }
    }


    @Operation(summary = "Atualizar usuário por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"id\": 1, \"email\": \"novo@email.com\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\": \"404\", \"error\": \"404 NOT_FOUND\", \"message\": \"Usuário não encontrado\"}")
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody @Valid UserRegisterDTO dto) {
        log.info("Atualizando usuário ID: {} com novo email: {}", id, dto.getEmail());
        try {
            Usuario atualizado = service.atualizar(id, dto);
            log.info("Usuário atualizado com sucesso: id={}, email={}", atualizado.getId(), atualizado.getEmail());
            return ResponseEntity.ok(atualizado);
        } catch (ResponseStatusException ex) {
            log.warn("Erro ao atualizar usuário ID {}: {}", id, ex.getReason());
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", String.valueOf(ex.getStatusCode().value()),
                    "error", ex.getStatusCode().toString(),
                    "message", ex.getReason() != null ? ex.getReason() : "Erro inesperado"
            ));
        }
    }

    @Operation(summary = "Excluir usuário por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\": \"500\", \"error\": \"500 INTERNAL_SERVER_ERROR\", \"message\": \"Erro inesperado\"}")
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.info("Solicitação para deletar usuário ID: {}", id);
        service.deletar(id);
        log.info("Usuário deletado com sucesso: ID {}", id);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Buscar o perfil de investidor do usuário por e-mail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil de investidor retornado com sucesso",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"perfilInvestidor\": \"Agressivo\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\": \"404\", \"error\": \"404 NOT_FOUND\", \"message\": \"Usuário não encontrado\"}")
                    )
            )
    })
    @GetMapping("/perfil/{email}")
    public ResponseEntity<PerfilDTO> buscarPerfilPorEmail(@PathVariable String email) {
        String perfil = service.buscarPerfilInvestidorPorEmail(email);
        return ResponseEntity.ok(new PerfilDTO(perfil));
    }


    @Operation(summary = "Atualizar o perfil de investidor do usuário por e-mail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"perfilInvestidor\": \"Moderado\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\": \"404\", \"error\": \"404 NOT_FOUND\", \"message\": \"Usuário não encontrado\"}")
                    )
            )
    })
    @PutMapping("/perfil/{email}")
    public ResponseEntity<PerfilDTO> atualizarPerfilInvestidorPorEmail(@PathVariable String email, @RequestBody PerfilDTO dto) {
        Usuario atualizado = service.atualizarPerfilInvestidorPorEmail(email, dto.getPerfilInvestidor());
        return ResponseEntity.ok(new PerfilDTO(atualizado.getPerfilInvestidor()));
    }



}
