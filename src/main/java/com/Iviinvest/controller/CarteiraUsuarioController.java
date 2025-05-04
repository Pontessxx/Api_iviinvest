package com.Iviinvest.controller;

import com.Iviinvest.model.*;
import com.Iviinvest.repository.CarteiraAtivoRepository;
import com.Iviinvest.repository.CarteiraPercentualRepository;
import com.Iviinvest.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.Data;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/carteiras")
public class CarteiraUsuarioController {

    private final CarteiraUsuarioService carteiraService;
    private final UsuarioService usuarioService;
    private final ObjetivoUsuarioService objetivoService;
    private final CarteiraPercentualRepository carteiraPercentualRepository;
    private final CarteiraAtivoRepository carteiraAtivoRepository;
    private final IAService iaService;
    private final CarteiraAtivoService carteiraAtivoService;

    public CarteiraUsuarioController(
            CarteiraUsuarioService carteiraService,
            UsuarioService usuarioService,
            ObjetivoUsuarioService objetivoService,
            CarteiraPercentualRepository carteiraPercentualRepository,
            CarteiraAtivoRepository carteiraAtivoRepository,
            IAService iaService,
            CarteiraAtivoService carteiraAtivoService) {

        this.carteiraService = carteiraService;
        this.usuarioService = usuarioService;
        this.objetivoService = objetivoService;
        this.carteiraPercentualRepository = carteiraPercentualRepository;
        this.carteiraAtivoRepository = carteiraAtivoRepository;
        this.iaService = iaService;
        this.carteiraAtivoService = carteiraAtivoService;
    }

    @PostMapping("/simular-percentual")
    @Operation(summary = "Simular apenas as distribuições percentuais das carteiras", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> simularDistribuicao(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        try {
            Usuario usuario = usuarioService.findByEmail(userDetails.getUsername());
            ObjetivoUsuario objetivo = objetivoService.buscarUltimoPorUsuario(usuario)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum objetivo encontrado"));

            String prompt = iaService.gerarPromptDistribuicao(objetivo);
            JSONObject distribuicao = iaService.chamarOpenAI(prompt);

            JSONObject distConservadora = distribuicao.getJSONObject("conservadora");
            JSONObject distAgressiva = distribuicao.getJSONObject("agressiva");

            distConservadora.keySet().forEach(segmento -> {
                CarteiraPercentual cp = carteiraPercentualRepository
                        .findByUsuarioIdAndObjetivoIdAndTipoCarteiraAndSegmento(usuario.getId(), objetivo.getId(), "conservadora", segmento)
                        .orElse(new CarteiraPercentual());

                cp.setUsuario(usuario);
                cp.setObjetivo(objetivo);
                cp.setTipoCarteira("conservadora");
                cp.setSegmento(segmento);
                cp.setPercentual(distConservadora.getInt(segmento));
                carteiraPercentualRepository.save(cp);
            });

            distAgressiva.keySet().forEach(segmento -> {
                CarteiraPercentual cp = carteiraPercentualRepository
                        .findByUsuarioIdAndObjetivoIdAndTipoCarteiraAndSegmento(usuario.getId(), objetivo.getId(), "agressiva", segmento)
                        .orElse(new CarteiraPercentual());

                cp.setUsuario(usuario);
                cp.setObjetivo(objetivo);
                cp.setTipoCarteira("agressiva");
                cp.setSegmento(segmento);
                cp.setPercentual(distAgressiva.getInt(segmento));
                carteiraPercentualRepository.save(cp);
            });

            return ResponseEntity.ok(Map.of(
                    "message", "Distribuições geradas com sucesso",
                    "distribuicao", distribuicao.toMap()
            ));

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erro ao gerar distribuições: " + ex.getMessage()
            ));
        }
    }


    @PostMapping("/selecionar-carteira")
    @Operation(summary = "Confirmar escolha de carteira com base no tipo (0: conservadora, 1: agressiva)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> confirmarEscolhaCarteira(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails,
            @RequestBody EscolhaCarteiraDTO dto) {
        try {
            Usuario usuario = usuarioService.findByEmail(userDetails.getUsername());
            ObjetivoUsuario objetivo = objetivoService.buscarUltimoPorUsuario(usuario)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Objetivo não encontrado"));

            String tipoCarteira = dto.getTipoCarteira() == 0 ? "conservadora" : "agressiva";

            List<CarteiraPercentual> percentuais = carteiraPercentualRepository
                    .findByUsuarioIdAndObjetivoId(usuario.getId(), objetivo.getId())
                    .stream()
                    .filter(p -> p.getTipoCarteira().equalsIgnoreCase(tipoCarteira))
                    .toList();

            JSONObject distribuicao = new JSONObject();
            for (CarteiraPercentual cp : percentuais) {
                distribuicao.put(cp.getSegmento(), cp.getPercentual());
            }

            String prompt = iaService.gerarPromptAtivos(objetivo, new JSONObject(Map.of(tipoCarteira, distribuicao)), tipoCarteira);
            JSONObject carteiraJson = iaService.chamarOpenAI(prompt).getJSONObject("carteira");

            carteiraAtivoService.salvarAtivos(tipoCarteira, carteiraJson, usuario, objetivo,
                    new JSONObject(Map.of(tipoCarteira, distribuicao)));

            CarteiraUsuario carteira = carteiraService.buscarPorObjetivo(objetivo).orElse(new CarteiraUsuario());
            carteira.setUsuario(usuario);
            carteira.setObjetivoUsuario(objetivo);
            carteira.setCarteiraSelecionada(tipoCarteira);

            if (dto.getTipoCarteira() == 0) {
                carteira.setCarteiraConservadoraJson(carteiraJson.toString());
            } else {
                carteira.setCarteiraAgressivaJson(carteiraJson.toString());
            }

            carteiraService.salvar(carteira);

            return ResponseEntity.ok(Map.of(
                    "message", "Carteira confirmada com sucesso",
                    "carteiraEscolhida", tipoCarteira,
                    "carteira", carteiraJson.toMap()
            ));

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erro ao confirmar carteira: " + ex.getMessage()
            ));
        }
    }

    @GetMapping("/selecionar")
    @Operation(
            summary = "Visualizar carteira confirmada do usuário",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> visualizarCarteiraSelecionada(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        // 1) Busca usuário e objetivo
        Usuario usuario = usuarioService.findByEmail(userDetails.getUsername());
        ObjetivoUsuario objetivo = objetivoService
                .buscarUltimoPorUsuario(usuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nenhum objetivo encontrado para o usuário"));

        // 2) Busca a carteira salva (entidade que contém os JSONs brutos) :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}
        CarteiraUsuario carteira = carteiraService
                .buscarPorObjetivo(objetivo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nenhuma carteira confirmada para este objetivo"));

        // 3) Decide qual JSON retornar
        String tipo = carteira.getCarteiraSelecionada(); // "conservadora" ou "agressiva"
        String jsonRaw = switch (tipo.toLowerCase()) {
            case "conservadora" -> carteira.getCarteiraConservadoraJson();
            case "agressiva"    -> carteira.getCarteiraAgressivaJson();
            default -> throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Tipo de carteira inválido: " + tipo);
        };

        // 4) Converte para Map e devolve
        JSONObject carteiraJson = new JSONObject(jsonRaw);
        return ResponseEntity.ok(Map.of("carteira", carteiraJson.toMap()));
    }


    @Data
    public static class EscolhaCarteiraDTO {
        private int tipoCarteira;
    }
}
