package com.Iviinvest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.Iviinvest.dto.ObjetivoUsuarioDTO;
import com.Iviinvest.model.ObjetivoUsuario;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.repository.ObjetivoUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ObjetivoUsuarioService {

    private final ObjetivoUsuarioRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ObjetivoUsuarioService(ObjetivoUsuarioRepository repository) {
        this.repository = repository;
    }

    public ObjetivoUsuario salvarExistente(ObjetivoUsuario objetivo) {
        return repository.save(objetivo);
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
        objetivo.setDataCriacao(dto.getDataCriacao());

        try {
            objetivo.setSetoresEvitar(objectMapper.writeValueAsString(dto.getSetoresEvitar()));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter setores para JSON", e);
        }

        return repository.save(objetivo);
    }

    public Optional<ObjetivoUsuario> buscarUltimoPorUsuario(Usuario usuario) {
        return repository.findFirstByUsuarioOrderByIdDesc(usuario);
    }

    public List<ObjetivoUsuarioDTO> buscarHistoricoPorUsuario(Usuario usuario) {
        List<ObjetivoUsuario> objetivos = repository.findAllByUsuarioOrderByIdDesc(usuario);

        return objetivos.stream().map(objetivo -> {
            try {
                List<String> setoresEvitar = objectMapper.readValue(objetivo.getSetoresEvitar(), new TypeReference<>() {});
                ObjetivoUsuarioDTO dto = new ObjetivoUsuarioDTO();
                dto.setObjetivo(objetivo.getObjetivo());
                dto.setPrazo(objetivo.getPrazo());
                dto.setValorInicial(objetivo.getValorInicial());
                dto.setAporteMensal(objetivo.getAporteMensal());
                dto.setPatrimonioAtual(objetivo.getPatrimonioAtual());
                dto.setLiquidez(objetivo.getLiquidez());
                dto.setSetoresEvitar(setoresEvitar);
                dto.setDataCriacao(dto.getDataCriacao());
                return dto;
            } catch (Exception e) {
                throw new RuntimeException("Erro ao converter setores evitados", e);
            }
        }).toList();
    }
    public ObjetivoUsuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Objetivo não encontrado"));
    }
    public Optional<ObjetivoUsuario> buscarPorIdEUsuario(Long id, Usuario usuario) {
        return repository.findByIdAndUsuario(id, usuario);
    }
}
