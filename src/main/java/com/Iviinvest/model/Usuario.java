package com.Iviinvest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String senhaHash;

    private String tokenReset;

    @Column(name = "perfil_investidor", nullable = true)
    private String perfilInvestidor;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getTokenReset() {
        return tokenReset;
    }

    public void setTokenReset(String tokenReset) {
        this.tokenReset = tokenReset;
    }

    public String getPerfilInvestidor() {
        return perfilInvestidor;
    }

    public void setPerfilInvestidor(String perfilInvestidor) {
        this.perfilInvestidor = perfilInvestidor;
    }
}
