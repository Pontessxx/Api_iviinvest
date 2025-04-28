package com.Iviinvest.repository;

import com.Iviinvest.model.ObjetivoUsuario;
import com.Iviinvest.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ObjetivoUsuarioRepository extends JpaRepository<ObjetivoUsuario, Long> {
    Optional<ObjetivoUsuario> findByUsuario(Usuario usuario);
}
