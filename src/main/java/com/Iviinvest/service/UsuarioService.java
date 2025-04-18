package com.Iviinvest.service;

import com.Iviinvest.dto.LoginDTO;
import com.Iviinvest.dto.UserRegisterDTO;
import com.Iviinvest.dto.UsuarioPublicDTO;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    private EmailService emailService;

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
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + 86400000); // 24h

        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes())
                .compact();
    }


    public void gerarTokenReset(String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        String token = UUID.randomUUID().toString();
        String hashedToken = com.Iviinvest.util.HashUtil.sha256(token); // ajusta import se necessário

        usuario.setTokenReset(hashedToken);
        repository.save(usuario);

        // Conteúdo do e-mail
        String assunto = "Recuperação de Senha - Ivi Invest";
        String corpo = "Olá,\n\nVocê solicitou a redefinição de senha para sua conta.\n" +
                "Use o token abaixo para redefinir sua senha:\n\n" +
                token + "\n\n" +
                "Se você não fez essa solicitação, apenas ignore este e-mail.\n\n" +
                "Atenciosamente,\nEquipe Ivi Invest";

        emailService.enviar(email, assunto, corpo);
        log.info("Token de redefinição enviado para o e-mail: {}", email);
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
        usuarios.forEach(u -> u.setTokenReset(null));
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

    public List<UsuarioPublicDTO> listarTodosUsuariosPublicos() {
        return repository.findAll().stream()
                .map(usuario -> new UsuarioPublicDTO(usuario.getId()))
                .collect(Collectors.toList());
    }

    public UsuarioPublicDTO buscarUsuarioPublicoPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return new UsuarioPublicDTO(usuario.getId());
    }




}
