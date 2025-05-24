package com.Iviinvest.controller;

import com.Iviinvest.dto.LoginDTO;
import com.Iviinvest.dto.UserRegisterDTO;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioControllerTest {

    private UsuarioService usuarioService;
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        usuarioController = new UsuarioController(usuarioService);
    }

    @Test
    void deveCadastrarUsuarioComSucesso() {
        // Arrange
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setEmail("teste@teste.com");
        dto.setSenha("senha123");

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setEmail(dto.getEmail());

        when(usuarioService.cadastrarUsuario(dto)).thenReturn(usuarioSalvo);
        when(usuarioService.autenticar(new LoginDTO(dto.getEmail(), dto.getSenha()))).thenReturn("jwt-token-mock");

        // Act
        ResponseEntity<?> response = usuarioController.cadastrar(dto);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.get("id"));
        assertEquals("teste@teste.com", body.get("email"));
        assertEquals("jwt-token-mock", body.get("token"));
    }

    @Test
    void deveTratarExcecaoDuranteCadastro() {
        // Arrange
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setEmail("teste@teste.com");
        dto.setSenha("senha123");

        when(usuarioService.cadastrarUsuario(dto)).thenThrow(
                new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Email já cadastrado"));

        // Act
        ResponseEntity<?> response = usuarioController.cadastrar(dto);

        // Assert
        assertEquals(409, response.getStatusCodeValue()); // CONFLICT
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("409", String.valueOf(body.get("status")));
        assertTrue(body.get("message").toString().contains("Email já cadastrado"));
    }
}
