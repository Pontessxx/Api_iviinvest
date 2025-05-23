package com.Iviinvest.repository;

import com.Iviinvest.model.ObjetivoUsuario;
import com.Iviinvest.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ObjetivoUsuarioRepository extends JpaRepository<ObjetivoUsuario, Long> {
    Optional<ObjetivoUsuario> findFirstByUsuarioOrderByIdDesc(Usuario usuario);
    List<ObjetivoUsuario> findAllByUsuarioOrderByIdDesc(Usuario usuario);
    Optional<ObjetivoUsuario> findByIdAndUsuario(Long id, Usuario usuario);
}
