package com.Iviinvest.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro JWT para autenticação de requisições HTTP.
 * <p>
 * Este filtro intercepta requisições e valida tokens JWT no cabeçalho Authorization.
 * Se válido, configura o contexto de segurança do Spring.
 * <p>
 * JWT filter for HTTP request authentication.
 * Intercepts requests and validates JWT tokens in the Authorization header.
 * If valid, configures Spring's security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret; // Chave secreta para validação JWT | Secret key for JWT validation

    /**
     * Método principal que filtra cada requisição.
     * <p>
     * Extrai e valida o token JWT, configurando a autenticação no contexto de segurança.
     * <p>
     * Main method that filters each request.
     * Extracts and validates JWT token, setting authentication in security context.
     *
     * @param request Objeto HttpServletRequest | HttpServletRequest object
     * @param response Objeto HttpServletResponse | HttpServletResponse object
     * @param filterChain Cadeia de filtros | Filter chain
     * @throws ServletException Em caso de erro no servlet | In case of servlet error
     * @throws IOException Em caso de erro de I/O | In case of I/O error
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extrai o cabeçalho Authorization | Extract Authorization header
        final String header = request.getHeader("Authorization");

        // 2. Verifica se o cabeçalho é válido | Check if header is valid
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrai o token (remove "Bearer ") | Extract token (remove "Bearer ")
        final String token = header.substring(7);
        Claims claims;

        try {
            // 4. Valida e decodifica o token JWT | Validate and decode JWT token
            claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // 5. Token inválido - continua a cadeia de filtros sem autenticação
            // Invalid token - continues filter chain without authentication
            filterChain.doFilter(request, response);
            return;
        }

        // 6. Extrai o email (subject) do token | Extract email (subject) from token
        String email = claims.getSubject();

        // 7. Cria objeto de autenticação | Create authentication object
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        new User(email, "", Collections.emptyList()), // User principal
                        null, // Credenciais (não necessárias para JWT) | Credentials (not needed for JWT)
                        Collections.emptyList() // Autorizações (roles) | Authorities (roles)
                );

        // 8. Adiciona detalhes da requisição | Add request details
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 9. Configura o contexto de segurança | Set security context
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 10. Continua a cadeia de filtros | Continue filter chain
        filterChain.doFilter(request, response);
    }
}