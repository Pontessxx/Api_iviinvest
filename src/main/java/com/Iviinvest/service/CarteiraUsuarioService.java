package com.Iviinvest.service;

import com.Iviinvest.model.CarteiraUsuario;
import com.Iviinvest.model.ObjetivoUsuario;
import com.Iviinvest.repository.CarteiraUsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CarteiraUsuarioService {

    private final CarteiraUsuarioRepository repository;

    public CarteiraUsuarioService(CarteiraUsuarioRepository repository) {
        this.repository = repository;
    }

    public CarteiraUsuario salvar(CarteiraUsuario carteiraUsuario) {
        return repository.save(carteiraUsuario);
    }

    public Optional<CarteiraUsuario> buscarPorObjetivo(ObjetivoUsuario objetivoUsuario) {
        return repository.findByObjetivoUsuario(objetivoUsuario);
    }

    public void excluir(CarteiraUsuario carteira) {
        repository.delete(carteira);
    }

}
