package com.Iviinvest.model;

import jakarta.persistence.*;

@Entity
public class CarteiraPercentual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String segmento;
    private Integer percentual;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private ObjetivoUsuario objetivo;

    @Column(nullable = false)
    private String tipoCarteira;


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

    public Integer getPercentual() {
        return percentual;
    }

    public void setPercentual(Integer percentual) {
        this.percentual = percentual;
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
}
