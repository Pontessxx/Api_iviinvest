package com.Iviinvest.service;

import com.Iviinvest.dto.ObjetivoUsuarioDTO;
import com.Iviinvest.model.ObjetivoUsuario;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.repository.ObjetivoUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ObjetivoUsuarioService {

    private final ObjetivoUsuarioRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ObjetivoUsuarioService(ObjetivoUsuarioRepository repository) {
        this.repository = repository;
    }

    public ObjetivoUsuario salvarObjetivo(Usuario usuario, ObjetivoUsuarioDTO dto) {
        ObjetivoUsuario objetivo = new ObjetivoUsuario();
        objetivo.setUsuario(usuario);
        objetivo.setObjetivo(dto.getObjetivo());
        objetivo.setPrazo(dto.getPrazo());
        objetivo.setValorInicial(dto.getValorInicial());
        objetivo.setAporteMensal(dto.getAporteMensal());
        objetivo.setPatrimonioAtual(dto.getPatrimonioAtual());
        objetivo.setLiquidez(dto.getLiquidez());

        try {
            objetivo.setSetoresEvitar(objectMapper.writeValueAsString(dto.getSetoresEvitar()));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter setores para JSON", e);
        }

        return repository.save(objetivo);
    }

    public Optional<ObjetivoUsuario> buscarPorUsuario(Usuario usuario) {
        return repository.findByUsuario(usuario);
    }
}
