package com.Iviinvest.model;

import jakarta.persistence.*;

@Entity
public class CarteiraAtivo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String segmento;
    private String nomeAtivo;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private ObjetivoUsuario objetivo;

    @Column(nullable = false)
    private String tipoCarteira; // Ex: "Conservadora", "Agressiva" ou "Alta Liquidez", etc.

    @Column(nullable = false)
    private Double precoUnitario;

    @Column(nullable = false)
    private Integer quantidadeCotas;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public String getNomeAtivo() {
        return nomeAtivo;
    }

    public void setNomeAtivo(String nomeAtivo) {
        this.nomeAtivo = nomeAtivo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ObjetivoUsuario getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(ObjetivoUsuario objetivo) {
        this.objetivo = objetivo;
    }

    public String getTipoCarteira() {
        return tipoCarteira;
    }

    public void setTipoCarteira(String tipoCarteira) {
        this.tipoCarteira = tipoCarteira;
    }

    public Double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(Double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public Integer getQuantidadeCotas() {
        return quantidadeCotas;
    }

    public void setQuantidadeCotas(Integer quantidadeCotas) {
        this.quantidadeCotas = quantidadeCotas;
    }
}