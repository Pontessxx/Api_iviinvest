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

/**
 * Serviço para operações relacionadas a usuários.
 * <p>
 * Responsável por: cadastro, autenticação, gerenciamento de tokens,
 * redefinição de senha e operações CRUD de usuários.
 * <p>
 * Service for user-related operations.
 * Handles: registration, authentication, token management,
 * password reset and user CRUD operations.
 */
@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${jwt.secret}")
    private String jwtSecret; // Chave secreta para JWT | Secret key for JWT

    /**
     * Construtor para injeção de dependências.
     *
     * @param repository Repositório de usuários
     * @param passwordEncoder Codificador de senhas
     *
     * Constructor for dependency injection.
     *
     * @param repository User repository
     * @param passwordEncoder Password encoder
     */
    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cadastra um novo usuário no sistema.
     *
     * @param dto DTO com dados de registro
     * @return Usuário cadastrado
     *
     * Registers a new user in the system.
     *
     * @param dto Registration DTO
     * @return Registered user
     */
    public Usuario cadastrarUsuario(UserRegisterDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        log.info("[CADASTRO] Usuário {} cadastrado", dto.getEmail());
        return repository.save(usuario);
    }

    /**
     * Autentica um usuário e gera token JWT.
     *
     * @param loginDTO DTO com credenciais de login
     * @return Token JWT
     * @throws ResponseStatusException Se credenciais inválidas
     *
     * Authenticates user and generates JWT token.
     *
     * @param loginDTO Login credentials DTO
     * @return JWT token
     * @throws ResponseStatusException If invalid credentials
     */
    public String autenticar(LoginDTO loginDTO) {
        Usuario usuario = repository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> {
                    log.warn("[AUTH] Tentativa de login com email não cadastrado: {}", loginDTO.getEmail());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
                });

        if (!passwordEncoder.matches(loginDTO.getSenha(), usuario.getSenhaHash())) {
            log.warn("[AUTH] Senha incorreta para usuário: {}", loginDTO.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha incorreta");
        }

        log.info("[AUTH] Login bem-sucedido para: {}", loginDTO.getEmail());
        return gerarTokenJWT(usuario);
    }

    /**
     * Gera token JWT para autenticação.
     *
     * @param usuario Usuário autenticado
     * @return Token JWT
     *
     * Generates JWT token for authentication.
     *
     * @param usuario Authenticated user
     * @return JWT token
     */
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

    /**
     * Gera e envia token para redefinição de senha.
     *
     * @param email Email do usuário
     * @throws ResponseStatusException Se usuário não encontrado
     *
     * Generates and sends password reset token.
     *
     * @param email User email
     * @throws ResponseStatusException If user not found
     */
    public void gerarTokenReset(String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[PASSWORD_RESET] Email não encontrado: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
                });

        String token = UUID.randomUUID().toString();
        String hashedToken = com.Iviinvest.util.HashUtil.sha256(token);

        usuario.setTokenReset(hashedToken);
        repository.save(usuario);

        String assunto = "Recuperação de Senha - Ivi Invest";
        String corpo = String.format(
                "Olá,\n\nVocê solicitou a redefinição de senha.\nToken: %s\n\nEquipe Ivi Invest",
                token
        );

        emailService.enviar(email, assunto, corpo);
        log.info("[PASSWORD_RESET] Token enviado para: {}", email);
    }

    /**
     * Redefine a senha do usuário usando token válido.
     *
     * @param token Token de redefinição
     * @param novaSenha Nova senha
     * @throws ResponseStatusException Se token inválido
     *
     * Resets user password using valid token.
     *
     * @param token Reset token
     * @param novaSenha New password
     * @throws ResponseStatusException If invalid token
     */
    public void redefinirSenha(String token, String novaSenha) {
        Usuario usuario = repository.findAll().stream()
                .filter(u -> token.equals(u.getTokenReset()))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("[PASSWORD_RESET] Token inválido: {}", token);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido");
                });

        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setTokenReset(null);
        repository.save(usuario);
        log.info("[PASSWORD_RESET] Senha alterada para: {}", usuario.getEmail());
    }

    // [CONTINUA COM OS DEMAIS MÉTODOS...]
    // [CONTINUE WITH OTHER METHODS...]

    /**
     * Lista todos os usuários (sem informações sensíveis).
     *
     * @return Lista de usuários
     *
     * Lists all users (without sensitive information).
     *
     * @return List of users
     */
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = repository.findAll();
        usuarios.forEach(u -> {
            u.setSenhaHash(null);
            u.setTokenReset(null);
        });
        log.info("[LISTAGEM] Listando todos os usuários");
        return usuarios;
    }

    /**
     * Busca usuário por ID (sem informações sensíveis).
     *
     * @param id ID do usuário
     * @return Usuário encontrado
     * @throws ResponseStatusException Se usuário não encontrado
     *
     * Finds user by ID (without sensitive information).
     *
     * @param id User ID
     * @return Found user
     * @throws ResponseStatusException If user not found
     */
    public Usuario buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[BUSCA] Usuário não encontrado: ID {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
                });
        usuario.setSenhaHash(null);
        return usuario;
    }

    /**
     * Atualiza informações do usuário.
     *
     * @param id ID do usuário
     * @param dto DTO com novos dados
     * @return Usuário atualizado
     *
     * Updates user information.
     *
     * @param id User ID
     * @param dto DTO with new data
     * @return Updated user
     */
    public Usuario atualizar(Long id, UserRegisterDTO dto) {
        Usuario usuario = buscarPorId(id);
        usuario.setEmail(dto.getEmail());
        usuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        log.info("[ATUALIZAÇÃO] Usuário atualizado: ID {}", id);
        return repository.save(usuario);
    }

    /**
     * Remove usuário do sistema.
     *
     * @param id ID do usuário
     *
     * Deletes user from system.
     *
     * @param id User ID
     */
    public void deletar(Long id) {
        repository.deleteById(id);
        log.info("[EXCLUSÃO] Usuário removido: ID {}", id);
    }

    /**
     * Lista todos os usuários (apenas dados públicos).
     *
     * @return Lista de DTOs públicos
     *
     * Lists all users (public data only).
     *
     * @return List of public DTOs
     */
    public List<UsuarioPublicDTO> listarTodosUsuariosPublicos() {
        return repository.findAll().stream()
                .map(usuario -> new UsuarioPublicDTO(usuario.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Busca usuário por ID (apenas dados públicos).
     *
     * @param id ID do usuário
     * @return DTO público do usuário
     * @throws RuntimeException Se usuário não encontrado
     *
     * Finds user by ID (public data only).
     *
     * @param id User ID
     * @return User public DTO
     * @throws RuntimeException If user not found
     */
    public UsuarioPublicDTO buscarUsuarioPublicoPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("[BUSCA_PUBLICA] Usuário não encontrado: ID {}", id);
                    return new RuntimeException("Usuário não encontrado");
                });
        return new UsuarioPublicDTO(usuario.getId());
    }

    /**
     * Obtém perfil de investidor por email.
     *
     * @param email Email do usuário
     * @return Perfil de investidor
     * @throws ResponseStatusException Se usuário não encontrado
     *
     * Gets investor profile by email.
     *
     * @param email User email
     * @return Investor profile
     * @throws ResponseStatusException If user not found
     */
    public String buscarPerfilInvestidorPorEmail(String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[PERFIL] Usuário não encontrado: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
                });
        return usuario.getPerfilInvestidor();
    }

    /**
     * Atualiza perfil de investidor por email.
     *
     * @param email Email do usuário
     * @param novoPerfil Novo perfil
     * @return Usuário atualizado
     * @throws ResponseStatusException Se usuário não encontrado
     *
     * Updates investor profile by email.
     *
     * @param email User email
     * @param novoPerfil New profile
     * @return Updated user
     * @throws ResponseStatusException If user not found
     */
    public Usuario atualizarPerfilInvestidorPorEmail(String email, String novoPerfil) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[PERFIL] Usuário não encontrado: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
                });
        usuario.setPerfilInvestidor(novoPerfil);
        log.info("[PERFIL] Perfil atualizado para: {}", email);
        return repository.save(usuario);
    }
}