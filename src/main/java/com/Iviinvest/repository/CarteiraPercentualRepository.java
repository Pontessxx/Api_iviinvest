package com.Iviinvest.repository;

import com.Iviinvest.model.CarteiraPercentual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface CarteiraPercentualRepository extends JpaRepository<CarteiraPercentual, Long> {

    boolean existsByUsuarioIdAndObjetivoId(Long usuarioId, Long objetivoId);
    List<CarteiraPercentual> findByUsuarioIdAndObjetivoId(Long usuarioId, Long objetivoId);
    @Transactional
    void deleteAllByUsuarioIdAndObjetivoId(Long usuarioId, Long objetivoId);
    Optional<CarteiraPercentual> findByUsuarioIdAndObjetivoIdAndTipoCarteiraAndSegmento(Long usuarioId, Long objetivoId, String tipoCarteira, String segmento);

    List<CarteiraPercentual> findByUsuarioIdAndObjetivoIdAndTipoCarteira(
            Long usuarioId,
            Long objetivoId,
            String tipoCarteira);
}
