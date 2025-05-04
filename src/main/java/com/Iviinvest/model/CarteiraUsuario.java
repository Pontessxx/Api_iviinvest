package com.Iviinvest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "carteiras_usuarios")
public class CarteiraUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com Usuário
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Relacionamento com Objetivo
    @OneToOne
    @JoinColumn(name = "objetivo_id", nullable = false)
    private ObjetivoUsuario objetivoUsuario;

    @Column(columnDefinition = "TEXT")
    private String carteiraConservadoraJson; // JSON bruto da carteira conservadora

    @Column(columnDefinition = "TEXT")
    private String carteiraAgressivaJson; // JSON bruto da carteira agressiva


    @Column
    private String carteiraSelecionada;

    // ============== GETTERS E SETTERS ==============
    // ============== GETTERS AND SETTERS ==============

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ObjetivoUsuario getObjetivoUsuario() {
        return objetivoUsuario;
    }

    public void setObjetivoUsuario(ObjetivoUsuario objetivoUsuario) {
        this.objetivoUsuario = objetivoUsuario;
    }

    public String getCarteiraConservadoraJson() {
        return carteiraConservadoraJson;
    }

    public void setCarteiraConservadoraJson(String carteiraConservadoraJson) {
        this.carteiraConservadoraJson = carteiraConservadoraJson;
    }

    public String getCarteiraAgressivaJson() {
        return carteiraAgressivaJson;
    }

    public void setCarteiraAgressivaJson(String carteiraAgressivaJson) {
        this.carteiraAgressivaJson = carteiraAgressivaJson;
    }

    public String getCarteiraSelecionada() {
        return carteiraSelecionada;
    }

    public void setCarteiraSelecionada(String carteiraSelecionada) {
        this.carteiraSelecionada = carteiraSelecionada;
    }
}
