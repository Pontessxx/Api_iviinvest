package com.Iviinvest.repository;

import com.Iviinvest.model.CarteiraAtivo;
import com.Iviinvest.model.ObjetivoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarteiraAtivoRepository extends JpaRepository<CarteiraAtivo, Long> {
    void deleteAllByUsuarioIdAndObjetivoId(Long usuarioId, Long objetivoId);

    // busca todos os ativos associados a um ObjetivoUsuario
    List<CarteiraAtivo> findByObjetivo(ObjetivoUsuario objetivo);

    List<CarteiraAtivo> findByObjetivoAndTipoCarteira(
            ObjetivoUsuario objetivo,
            String tipoCarteira
    );

}
