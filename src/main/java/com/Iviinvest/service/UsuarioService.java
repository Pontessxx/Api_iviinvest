package com.Iviinvest.service;

import com.Iviinvest.dto.LoginDTO;
import com.Iviinvest.dto.UserRegisterDTO;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrarUsuario(UserRegisterDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        log.info("Usuário {} cadastrado com sucesso", dto.getEmail());
        return repository.save(usuario);
    }

    public String autenticar(LoginDTO loginDTO) {
        Usuario usuario = repository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (!passwordEncoder.matches(loginDTO.getSenha(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha incorreta");
        }

        log.info("Usuário {} autenticado com sucesso", loginDTO.getEmail());
        return gerarTokenJWT(usuario);
    }

    private String gerarTokenJWT(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes())
                .compact();
    }

    public String gerarTokenReset(String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        String token = UUID.randomUUID().toString();
        usuario.setTokenReset(token);
        repository.save(usuario);
        log.info("Token de redefinição gerado para {}", email);
        return token;
    }

    public void redefinirSenha(String token, String novaSenha) {
        Usuario usuario = repository.findAll().stream()
                .filter(u -> token.equals(u.getTokenReset()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido"));

        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setTokenReset(null);
        repository.save(usuario);
        log.info("Senha redefinida com sucesso para {}", usuario.getEmail());
    }

    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = repository.findAll();
        usuarios.forEach(u -> u.setSenhaHash(null));
        return usuarios;
    }

    public Usuario buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        usuario.setSenhaHash(null);
        return usuario;
    }

    public Usuario atualizar(Long id, UserRegisterDTO dto) {
        Usuario usuario = buscarPorId(id);
        usuario.setEmail(dto.getEmail());
        usuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        return repository.save(usuario);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
        log.info("Usuário com id {} deletado", id);
    }





}
