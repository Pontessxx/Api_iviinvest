// service/JwtService.java
package com.Iviinvest.service;

import com.Iviinvest.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generateToken(Usuario usuario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + 86400000); // 24 horas

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(key)
                .compact();
    }
}
