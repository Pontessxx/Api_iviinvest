package com.Iviinvest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "objetivos_usuarios")
public class ObjetivoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Associação com o usuário
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Campos do formulário
    private String objetivo;
    private String prazo;
    private Double valorInicial;
    private Double aporteMensal;
    private Double patrimonioAtual;
    private String liquidez;

    @Column(length = 1000)
    private String setoresEvitar; // Armazena como JSON String


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

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getPrazo() {
        return prazo;
    }

    public void setPrazo(String prazo) {
        this.prazo = prazo;
    }

    public Double getValorInicial() {
        return valorInicial;
    }

    public void setValorInicial(Double valorInicial) {
        this.valorInicial = valorInicial;
    }

    public Double getAporteMensal() {
        return aporteMensal;
    }

    public void setAporteMensal(Double aporteMensal) {
        this.aporteMensal = aporteMensal;
    }

    public Double getPatrimonioAtual() {
        return patrimonioAtual;
    }

    public void setPatrimonioAtual(Double patrimonioAtual) {
        this.patrimonioAtual = patrimonioAtual;
    }

    public String getLiquidez() {
        return liquidez;
    }

    public void setLiquidez(String liquidez) {
        this.liquidez = liquidez;
    }

    public String getSetoresEvitar() {
        return setoresEvitar;
    }

    public void setSetoresEvitar(String setoresEvitar) {
        this.setoresEvitar = setoresEvitar;
    }
}
