package com.Iviinvest.repository;

import com.Iviinvest.model.CarteiraPercentual;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarteiraPercentualRepository extends JpaRepository<CarteiraPercentual, Long> {

    boolean existsByUsuarioIdAndObjetivoId(Long usuarioId, Long objetivoId);

}
