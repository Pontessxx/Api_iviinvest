package com.Iviinvest.controller;

import com.Iviinvest.dto.CarteiraPersistRequest;
import com.Iviinvest.dto.CarteiraSelecionadaRequest;
import org.springframework.transaction.annotation.Transactional;
import com.Iviinvest.model.*;
import com.Iviinvest.repository.CarteiraPercentualRepository;
import com.Iviinvest.service.IAService;
import com.Iviinvest.service.ObjetivoUsuarioService;
import com.Iviinvest.service.UsuarioService;
import com.Iviinvest.service.CarteiraUsuarioService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import com.Iviinvest.service.CarteiraAtivoService;
import com.Iviinvest.service.PrecoAtivoService;

import com.Iviinvest.dto.CarteiraResponseDTO;
import com.Iviinvest.service.CarteiraAtivoService;
import com.Iviinvest.service.PrecoAtivoService;
import java.util.stream.Collectors;



import java.util.*;

@RestController
@RequestMapping("/api/carteiras")
public class CarteiraUsuarioController {

    private static final Logger log = LoggerFactory.getLogger(CarteiraUsuarioController.class);

    private final IAService iaService;
    private final UsuarioService usuarioService;
    private final ObjetivoUsuarioService objetivoService;
    private final CarteiraPercentualRepository percentualRepo;
    private final CarteiraUsuarioService usuarioCarteiraService;
    private final CarteiraAtivoService carteiraAtivoService;
    private final PrecoAtivoService   precoAtivoService;



    public CarteiraUsuarioController(
            IAService iaService,
            UsuarioService usuarioService,
            ObjetivoUsuarioService objetivoService,
            CarteiraPercentualRepository percentualRepo,
            CarteiraUsuarioService usuarioCarteiraService,
            CarteiraAtivoService carteiraAtivoService,
            PrecoAtivoService precoAtivoService
    ) {
        this.iaService              = iaService;
        this.usuarioService         = usuarioService;
        this.objetivoService        = objetivoService;
        this.percentualRepo         = percentualRepo;
        this.usuarioCarteiraService = usuarioCarteiraService;
        this.carteiraAtivoService   = carteiraAtivoService;
        this.precoAtivoService      = precoAtivoService;
    }


    /**
     * 1) Gera percentuais ideais (conservadora + agressiva) via IA
     */
    @PostMapping("/percentuais/gerar")
    public ResponseEntity<?> gerarPercentuais(
            @AuthenticationPrincipal User userDetails
    ) throws Exception {
        Usuario u = usuarioService.findByEmail(userDetails.getUsername());
        ObjetivoUsuario obj = objetivoService
                .buscarUltimoPorUsuario(u)
                .orElseThrow(() -> new RuntimeException("Objetivo não encontrado"));

        // chama IA
        String promptPct = iaService.gerarPromptDistribuicao(obj);
        JSONObject respPct = iaService.chamarOpenAI(promptPct);

        // converte JSONObject → Map<String,Map<String,Integer>>
        Map<String, Map<String,Integer>> pctMap = new LinkedHashMap<>();
        for (String tipo : List.of("conservadora", "agressiva")) {
            JSONObject j = respPct.getJSONObject(tipo);
            Map<String,Integer> segMap = new LinkedHashMap<>();
            for (String segmento : j.keySet()) {
                segMap.put(segmento, j.getInt(segmento));
            }
            pctMap.put(tipo, segMap);
        }

        return ResponseEntity.ok(pctMap);
    }

    /**
     * 2) Gera carteiras de ativos (listas de códigos) a partir de percentuais,
     *    sem persistir
     */
    @PostMapping("/ativos/gerar")
    public ResponseEntity<?> gerarAtivos(
            @AuthenticationPrincipal User userDetails,
            @RequestBody Map<String, Map<String,Integer>> distribuicao
    ) throws Exception {
        Usuario u = usuarioService.findByEmail(userDetails.getUsername());
        ObjetivoUsuario obj = objetivoService
                .buscarUltimoPorUsuario(u)
                .orElseThrow(() -> new RuntimeException("Objetivo não encontrado"));

        // chama IA para obter JSON com "carteira":{...}
        JSONObject allCarteiras = iaService
                .chamarOpenAI(
                        iaService.gerarPromptAtivos(
                                obj,
                                new JSONObject(distribuicao),
                                "ambas"  // pode ser "conservadora", "agressiva" ou "ambas"
                        )
                )
                .getJSONObject("carteira");

        // converte JSONObject → Map<String,Map<String,List<String>>>
        Map<String, Map<String,List<String>>> result = new LinkedHashMap<>();
        for (String tipo : List.of("conservadora", "agressiva")) {
            JSONObject cj = allCarteiras.getJSONObject(tipo);
            Map<String,List<String>> ativosPorSegmento = new LinkedHashMap<>();
            for (String segmento : cj.keySet()) {
                JSONArray arr = cj.getJSONArray(segmento);
                List<String> lista = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    lista.add(arr.getString(i));
                }
                ativosPorSegmento.put(segmento, lista);
            }
            result.put(tipo, ativosPorSegmento);
        }

        return ResponseEntity.ok(result);
    }
    @GetMapping("/selecionada")
    public ResponseEntity<?> getCarteiraSelecionada(
            @AuthenticationPrincipal User userDetails,
            @RequestParam String tipo   // "conservadora" ou "agressiva"
    ) {
        // 1) busca usuário e objetivo
        Usuario u = usuarioService.findByEmail(userDetails.getUsername());
        ObjetivoUsuario obj = objetivoService
                .buscarUltimoPorUsuario(u)
                .orElseThrow(() -> new RuntimeException("Objetivo não encontrado"));

        Long userId = u.getId(), objId = obj.getId();

        // 2) busca percentuais
        List<CarteiraPercentual> pctList = percentualRepo
                .findByUsuarioIdAndObjetivoIdAndTipoCarteira(userId, objId, tipo);

        Map<String,Integer> percentuais = pctList.stream()
                .collect(Collectors.toMap(
                        CarteiraPercentual::getSegmento,
                        CarteiraPercentual::getPercentual
                ));

        // 3) busca ativos
        List<CarteiraAtivo> ativosList = carteiraAtivoService
                .buscarPorObjetivoETipo(obj, tipo);

        Map<String,List<CarteiraResponseDTO.AtivoDTO>> ativos = ativosList.stream()
                .collect(Collectors.groupingBy(
                        CarteiraAtivo::getSegmento,
                        Collectors.mapping(a ->
                                        new CarteiraResponseDTO.AtivoDTO(
                                                a.getNomeAtivo(),
                                                a.getPrecoUnitario(),
                                                a.getQuantidadeCotas()
                                        ),
                                Collectors.toList()
                        )
                ));

        // 4) monta e retorna
        CarteiraResponseDTO response = new CarteiraResponseDTO(percentuais, ativos);
        return ResponseEntity.ok(response);
    }

    // Métodos auxiliares dentro do controller (pode mover para um service):
    private void salvarPercentuais(Usuario u, ObjetivoUsuario obj, String tipo,
                                   Map<String,Integer> mapaPct) {
        mapaPct.forEach((segmento, pct) -> {
            CarteiraPercentual cp = new CarteiraPercentual();
            cp.setUsuario(u);
            cp.setObjetivo(obj);
            cp.setTipoCarteira(tipo);
            cp.setSegmento(segmento);
            cp.setPercentual(pct);
            percentualRepo.save(cp);
        });
    }

    private void salvarAtivosManuais(Usuario u, ObjetivoUsuario obj, String tipo,
                                     Map<String,List<String>> ativosPorSegmento,
                                     Map<String,Integer> distribuicaoPct) {
        double valorTotal = obj.getValorInicial();
        ativosPorSegmento.forEach((segmento, lista) -> {
            int pctSegmento = distribuicaoPct.getOrDefault(segmento, 0);
            double valorSegmento = valorTotal * pctSegmento / 100.0;
            int qtdAtivos = lista.size();
            double valorPorAtivo = qtdAtivos>0 ? valorSegmento/qtdAtivos : 0;

            for (String ticker : lista) {
                double preco = precoAtivoService.buscarPreco(ticker);
                int quantidade = preco>0 ? (int)Math.floor(valorPorAtivo/preco) : 0;
                if (quantidade<=0) continue;

                CarteiraAtivo ca = new CarteiraAtivo();
                ca.setUsuario(u);
                ca.setObjetivo(obj);
                ca.setTipoCarteira(tipo);
                ca.setSegmento(segmento);
                ca.setNomeAtivo(ticker);
                ca.setPrecoUnitario(preco);
                ca.setQuantidadeCotas(quantidade);
                carteiraAtivoService.salvar(ca);
            }
        });
    }

    @PostMapping("/selecionar")
    @Transactional  // garante transação ativa para deleteAll + saves
    public ResponseEntity<?> selecionarCarteira(
            @AuthenticationPrincipal User userDetails,
            @RequestParam("tipo") String tipo,
            @RequestBody CarteiraPersistRequest request
    ) {
        // 0) checagem de body
        if (request.getPercentuais() == null || request.getPercentuais().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Campo 'percentuais' é obrigatório e não pode ser vazio.");
        }
        if (request.getAtivos() == null || request.getAtivos().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Campo 'ativos' é obrigatório e não pode ser vazio.");
        }

        // 1) busca usuário e objetivo…
        Usuario u = usuarioService.findByEmail(userDetails.getUsername());
        ObjetivoUsuario obj = objetivoService
                .buscarUltimoPorUsuario(u)
                .orElseThrow(() -> new RuntimeException("Objetivo não encontrado"));

        // 2) limpa registros antigos
        percentualRepo.deleteAllByUsuarioIdAndObjetivoId(u.getId(), obj.getId());
        carteiraAtivoService.deleteAllByUsuarioIdAndObjetivoId(u.getId(), obj.getId());

        // 3) persiste percentuais e ativos — agora garantido não ser nulo
        salvarPercentuais(u, obj, tipo, request.getPercentuais());
        salvarAtivosManuais(u, obj, tipo, request.getAtivos(), request.getPercentuais());

        return ResponseEntity.ok("Carteira '" + tipo + "' salva com sucesso.");
    }

}
