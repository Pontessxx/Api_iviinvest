package com.Iviinvest.service;

import com.Iviinvest.model.CarteiraPercentual;
import com.Iviinvest.model.CarteiraUsuario;
import com.Iviinvest.model.ObjetivoUsuario;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.repository.CarteiraPercentualRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarteiraOperacaoService {

    private final UsuarioService usuarioService;
    private final ObjetivoUsuarioService objetivoService;
    private final CarteiraPercentualRepository percentualRepo;
    private final IAService iaService;
    private final CarteiraAtivoService ativoService;
    private final CarteiraUsuarioService usuarioCarteiraService;

    public CarteiraOperacaoService(
            UsuarioService usuarioService,
            ObjetivoUsuarioService objetivoService,
            CarteiraPercentualRepository percentualRepo,
            IAService iaService,
            CarteiraAtivoService ativoService,
            CarteiraUsuarioService usuarioCarteiraService) {
        this.usuarioService         = usuarioService;
        this.objetivoService        = objetivoService;
        this.percentualRepo         = percentualRepo;
        this.iaService              = iaService;
        this.ativoService           = ativoService;
        this.usuarioCarteiraService = usuarioCarteiraService;
    }

    /**
     * Gera via IA e salva no banco as DUAS carteiras de ativos
     * (conservadora e agressiva) baseadas nas porcentagens já salvas.
     */
    @Transactional
    public Map<String, Map<String, List<String>>> gerarESalvar(String emailUsuario) throws Exception {
        // 1) usuário e objetivo
        Usuario u = usuarioService.findByEmail(emailUsuario);
        ObjetivoUsuario obj = objetivoService
                .buscarUltimoPorUsuario(u)
                .orElseThrow(() -> new RuntimeException("Objetivo não encontrado"));

        // 2) carrega percentuais e monta JSON de distribuição
        List<CarteiraPercentual> pctList = percentualRepo
                .findByUsuarioIdAndObjetivoId(u.getId(), obj.getId());

        JSONObject distribuicao = new JSONObject(Map.of(
                "conservadora", new JSONObject(
                        pctList.stream()
                                .filter(p -> "conservadora".equalsIgnoreCase(p.getTipoCarteira()))
                                .collect(Collectors.toMap(
                                        CarteiraPercentual::getSegmento,
                                        CarteiraPercentual::getPercentual
                                ))
                ),
                "agressiva", new JSONObject(
                        pctList.stream()
                                .filter(p -> "agressiva".equalsIgnoreCase(p.getTipoCarteira()))
                                .collect(Collectors.toMap(
                                        CarteiraPercentual::getSegmento,
                                        CarteiraPercentual::getPercentual
                                ))
                )
        ));

        // 3) chama a IA — devolve um único JSON com ambas as carteiras
        String prompt = iaService.gerarPromptAtivos(obj, distribuicao, null);
        JSONObject resposta   = iaService.chamarOpenAI(prompt);
        JSONObject allCarteiras = resposta.getJSONObject("carteira");

        // 4) persiste ativos para cada carteira
        ativoService.salvarAtivos(
                "conservadora",
                allCarteiras.getJSONObject("conservadora"),
                u, obj,
                distribuicao
        );
        ativoService.salvarAtivos(
                "agressiva",
                allCarteiras.getJSONObject("agressiva"),
                u, obj,
                distribuicao
        );

        // 5) salva/atualiza registro em CarteiraUsuario
        CarteiraUsuario cu = usuarioCarteiraService
                .buscarPorObjetivo(obj)
                .orElse(new CarteiraUsuario());
        cu.setUsuario(u);
        cu.setObjetivoUsuario(obj);
        cu.setCarteiraConservadoraJson(allCarteiras
                .getJSONObject("conservadora")
                .toString());
        cu.setCarteiraAgressivaJson(allCarteiras
                .getJSONObject("agressiva")
                .toString());
        // ainda sem seleção; só registramos o preview
        usuarioCarteiraService.salvar(cu);

        // 6) converte JSONObject → Map<String,List<String>> para retorno
        Map<String, List<String>> consMap = jsonParaListMap(allCarteiras.getJSONObject("conservadora"));
        Map<String, List<String>> aggMap  = jsonParaListMap(allCarteiras.getJSONObject("agressiva"));

        return Map.of(
                "conservadora", consMap,
                "agressiva",    aggMap
        );
    }

    /** Helper: transforma {"fiis":["HGLG11",...],...} num Map<segmento,List<ticker>> */
    private Map<String, List<String>> jsonParaListMap(JSONObject obj) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String segmento : obj.keySet()) {
            JSONArray arr = obj.getJSONArray(segmento);
            List<String> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
            result.put(segmento, list);
        }
        return result;
    }
}
