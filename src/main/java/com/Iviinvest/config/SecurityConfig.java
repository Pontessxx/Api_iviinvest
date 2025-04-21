package com.Iviinvest.config;

import com.Iviinvest.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configuração principal de segurança da aplicação.
 * <p>
 * Define as políticas de segurança, endpoints públicos, filtros de autenticação
 * e configuração do codificador de senhas.
 * <p>
 * Main application security configuration.
 * Defines security policies, public endpoints, authentication filters,
 * and password encoder configuration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Construtor para injeção de dependência do filtro JWT.
     *
     * @param jwtAuthenticationFilter Filtro de autenticação JWT
     *
     * Constructor for JWT authentication filter dependency injection.
     *
     * @param jwtAuthenticationFilter JWT authentication filter
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configura a cadeia de filtros de segurança HTTP.
     *
     * @param http Configuração do HttpSecurity
     * @return SecurityFilterChain configurado
     * @throws Exception em caso de erro na configuração
     *
     * Configures the HTTP security filter chain.
     *
     * @param http HttpSecurity configuration
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration error occurs
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF para APIs stateless
                // Disables CSRF for stateless APIs
                .csrf(csrf -> csrf.disable())

                // Configura as autorizações de requisições
                // Configures request authorizations
                .authorizeHttpRequests(auth -> auth
                        // Permite acesso ao console H2 (apenas para desenvolvimento)
                        // Allows access to H2 console (development only)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Permite acesso à documentação Swagger/OpenAPI
                        // Allows access to Swagger/OpenAPI documentation
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Permite acesso público aos endpoints de autenticação
                        // Allows public access to authentication endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

                        // Permite acesso público aos endpoints de recuperação de senha
                        // Allows public access to password recovery endpoints
                        .requestMatchers("/api/recover/**").permitAll()

                        // Todas as demais requisições requerem autenticação
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Configuração especial para permitir frames do H2 Console
                // Special configuration to allow H2 Console frames
                .headers(headers -> headers.frameOptions().disable())

                // Desabilita autenticação por formulário (não usamos login HTML)
                // Disables form login (we don't use HTML login)
                .formLogin(form -> form.disable())

                // Desabilita autenticação básica HTTP (usamos JWT)
                // Disables HTTP Basic authentication (we use JWT)
                .httpBasic(httpBasic -> httpBasic.disable())

                // Adiciona nosso filtro JWT antes do filtro de autenticação padrão
                // Adds our JWT filter before the default authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura o codificador de senhas BCrypt.
     *
     * @return Instância do PasswordEncoder com BCrypt
     *
     * Configures the BCrypt password encoder.
     *
     * @return PasswordEncoder instance with BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Usa BCrypt com força padrão (10)
        // Uses BCrypt with default strength (10)
        return new BCryptPasswordEncoder();
    }
}