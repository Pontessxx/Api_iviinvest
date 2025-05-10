package com.Iviinvest.service;

import com.Iviinvest.model.CarteiraAtivo;
import com.Iviinvest.model.ObjetivoUsuario;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.repository.CarteiraAtivoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarteiraAtivoService {

    private final CarteiraAtivoRepository carteiraAtivoRepository;
    private final PrecoAtivoService precoAtivoService;

    public CarteiraAtivoService(CarteiraAtivoRepository carteiraAtivoRepository, PrecoAtivoService precoAtivoService) {
        this.carteiraAtivoRepository = carteiraAtivoRepository;
        this.precoAtivoService = precoAtivoService;
    }

    public void salvarAtivos(String tipoCarteira,
                             JSONObject carteiraJson,
                             Usuario usuario,
                             ObjetivoUsuario objetivo,
                             JSONObject distribuicao) {

        String nomeCarteira = objetivo.getObjetivo() + " - " + objetivo.getPrazo();
        double valorTotal = objetivo.getValorInicial();

        for (String segmento : carteiraJson.keySet()) {

            // Ignora o segmento de renda fixa se necessário
            if (segmento.equalsIgnoreCase("rendaFixa")) {
                // pula títulos de renda fixa
                continue;
            }

            JSONArray ativos = carteiraJson.getJSONArray(segmento);
            int percentualSegmento = distribuicao.getJSONObject(tipoCarteira.toLowerCase()).optInt(segmento, 0);

            double valorSegmento = valorTotal * percentualSegmento / 100.0;
            int qtdAtivos = ativos.length();
            double valorPorAtivo = qtdAtivos > 0 ? valorSegmento / qtdAtivos : 0;

            for (int i = 0; i < qtdAtivos; i++) {
                String nomeAtivo = ativos.getString(i);
                double precoUnitario = precoAtivoService.buscarPreco(nomeAtivo);

                int quantidadeCotas = precoUnitario > 0
                        ? (int) Math.floor(valorPorAtivo / precoUnitario)
                        : 0;

                if (quantidadeCotas <= 0) continue;

                CarteiraAtivo ca = new CarteiraAtivo();
                ca.setSegmento(segmento);
                ca.setTipoCarteira(nomeCarteira);
                ca.setNomeAtivo(nomeAtivo);
                ca.setUsuario(usuario);
                ca.setObjetivo(objetivo);
                ca.setPrecoUnitario(precoUnitario);
                ca.setQuantidadeCotas(quantidadeCotas);

                carteiraAtivoRepository.save(ca);
            }
        }
    }

    @Transactional
    public void deleteAllByUsuarioIdAndObjetivoId(Long usuarioId, Long objetivoId) {
        carteiraAtivoRepository.deleteAllByUsuarioIdAndObjetivoId(usuarioId, objetivoId);
    }

    public void salvar(CarteiraAtivo ativo) {
        carteiraAtivoRepository.save(ativo);
    }
    public List<CarteiraAtivo> buscarPorObjetivoETipo(ObjetivoUsuario objetivo, String tipoCarteira) {
        return carteiraAtivoRepository.findByObjetivoAndTipoCarteira(objetivo, tipoCarteira);
    }

}
