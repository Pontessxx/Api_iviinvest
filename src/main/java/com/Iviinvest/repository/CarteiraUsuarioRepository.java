package com.Iviinvest.repository;

import com.Iviinvest.model.CarteiraUsuario;
import com.Iviinvest.model.ObjetivoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarteiraUsuarioRepository extends JpaRepository<CarteiraUsuario, Long> {
    Optional<CarteiraUsuario> findByObjetivoUsuario(ObjetivoUsuario objetivoUsuario);
}
