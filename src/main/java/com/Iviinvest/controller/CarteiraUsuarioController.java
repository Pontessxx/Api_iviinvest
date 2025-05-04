package com.Iviinvest.controller;

import com.Iviinvest.dto.ErrorResponseDTO;
import com.Iviinvest.model.CarteiraUsuario;
import com.Iviinvest.model.CarteiraPercentual;
import com.Iviinvest.model.ObjetivoUsuario;
import com.Iviinvest.model.Usuario;
import com.Iviinvest.repository.CarteiraPercentualRepository;
import com.Iviinvest.service.CarteiraAtivoService;
import com.Iviinvest.service.CarteiraUsuarioService;
import com.Iviinvest.service.IAService;
import com.Iviinvest.service.ObjetivoUsuarioService;
import com.Iviinvest.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.Data;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller responsável pelo fluxo de simulação, confirmação e visualização
 * das carteiras de investimento (conservadora/agressiva) do usuário.
 */
@RestController
@RequestMapping("/api/carteiras")
public class CarteiraUsuarioController {

    private static final Logger log = LoggerFactory.getLogger(CarteiraUsuarioController.class);

    private final CarteiraUsuarioService carteiraService;
    private final UsuarioService usuarioService;
    private final ObjetivoUsuarioService objetivoService;
    private final CarteiraPercentualRepository carteiraPercentualRepository;
    private final CarteiraAtivoService carteiraAtivoService;
    private final IAService iaService;

    public CarteiraUsuarioController(
            CarteiraUsuarioService carteiraService,
            UsuarioService usuarioService,
            ObjetivoUsuarioService objetivoService,
            CarteiraPercentualRepository carteiraPercentualRepository,
            CarteiraAtivoService carteiraAtivoService,
            IAService iaService) {
        this.carteiraService = carteiraService;
        this.usuarioService = usuarioService;
        this.objetivoService = objetivoService;
        this.carteiraPercentualRepository = carteiraPercentualRepository;
        this.carteiraAtivoService = carteiraAtivoService;
        this.iaService = iaService;
    }

    /**
     * Simula as distribuições percentuais para as carteiras conservadora e agressiva
     * do último objetivo do usuário autenticado.
     *
     * @param userDetails detalhes do usuário (extraídos do token JWT)
     * @return 200 + mapa de distribuições, 404 se não houver objetivo,
     *         ou 500 em caso de erro interno
     */
    @PostMapping("/simular-percentual")
    @Operation(
            summary = "Simular distribuições percentuais",
            description = "Gera via IA as porcentagens de alocação para carteiras conservadora e agressiva.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Distribuições geradas com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"message\": \"Distribuições geradas com sucesso\",\n" +
                                            "  \"distribuicao\": {\n" +
                                            "    \"conservadora\": { \"fiis\": 40, \"rendaFixa\": 60 },\n" +
                                            "    \"agressiva\":   { \"acoes\": 70, \"cripto\": 30 }\n" +
                                            "  }\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhum objetivo encontrado para este usuário",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{ \"status\": \"404\", \"error\": \"404 NOT_FOUND\", \"message\": \"Nenhum objetivo encontrado\" }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno ao gerar distribuições",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{ \"status\": \"500\", \"error\": \"500 INTERNAL_SERVER_ERROR\", \"message\": \"Erro ao gerar distribuições: ...\" }"
                            )
                    )
            )
    })
    public ResponseEntity<?> simularDistribuicao(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        log.info("[POST] /api/carteiras/simular-percentual - usuário={}", userDetails.getUsername());
        try {
            Usuario u = usuarioService.findByEmail(userDetails.getUsername());
            ObjetivoUsuario obj = objetivoService
                    .buscarUltimoPorUsuario(u)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Nenhum objetivo encontrado"));

            JSONObject distrib = iaService.chamarOpenAI(iaService.gerarPromptDistribuicao(obj));

            // Persiste conservadora
            JSONObject cons = distrib.getJSONObject("conservadora");
            cons.keySet().forEach(seg -> {
                CarteiraPercentual cp = carteiraPercentualRepository
                        .findByUsuarioIdAndObjetivoIdAndTipoCarteiraAndSegmento(
                                u.getId(), obj.getId(), "conservadora", seg)
                        .orElse(new CarteiraPercentual());
                cp.setUsuario(u);
                cp.setObjetivo(obj);
                cp.setTipoCarteira("conservadora");
                cp.setSegmento(seg);
                cp.setPercentual(cons.getInt(seg));
                carteiraPercentualRepository.save(cp);
            });

            // Persiste agressiva
            JSONObject aggr = distrib.getJSONObject("agressiva");
            aggr.keySet().forEach(seg -> {
                CarteiraPercentual cp = carteiraPercentualRepository
                        .findByUsuarioIdAndObjetivoIdAndTipoCarteiraAndSegmento(
                                u.getId(), obj.getId(), "agressiva", seg)
                        .orElse(new CarteiraPercentual());
                cp.setUsuario(u);
                cp.setObjetivo(obj);
                cp.setTipoCarteira("agressiva");
                cp.setSegmento(seg);
                cp.setPercentual(aggr.getInt(seg));
                carteiraPercentualRepository.save(cp);
            });

            log.info("Distribuições inseridas no BD para usuário={}", u.getId());
            return ResponseEntity.ok(Map.of(
                    "message", "Distribuições geradas com sucesso",
                    "distribuicao", distrib.toMap()
            ));

        } catch (ResponseStatusException ex) {
            log.warn("simular-percentual - {}", ex.getReason());
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of(
                            "status", ex.getStatusCode().value(),
                            "error",  ex.getStatusCode().toString(),
                            "message", ex.getReason()
                    ));
        } catch (Exception ex) {
            log.error("Erro interno em simular-percentual", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "error",  HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "message", "Erro ao gerar distribuições: " + ex.getMessage()
                    ));
        }
    }

    /**
     * Confirma a carteira (0 = conservadora, 1 = agressiva), gera ativos via IA
     * e salva a seleção no banco.
     *
     * @param userDetails detalhes do usuário (JWT)
     * @param dto         tipo de carteira escolhida
     * @return 200 + detalhes da carteira, ou 404/500 em erro
     */
    @PostMapping("/selecionar-carteira")
    @Operation(
            summary = "Confirmar escolha de carteira",
            description = "Salva os ativos recomendados pela IA para a carteira selecionada.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Carteira confirmada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"message\": \"Carteira confirmada com sucesso\",\n" +
                                            "  \"carteiraEscolhida\": \"conservadora\",\n" +
                                            "  \"carteira\": { \"fiis\": [\"HGLG11\"], \"acoes\": [\"ITUB4\"] }\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Objetivo não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{ \"status\": \"404\", \"error\": \"404 NOT_FOUND\", \"message\": \"Objetivo não encontrado\" }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno ao confirmar carteira",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{ \"status\": \"500\", \"error\": \"500 INTERNAL_SERVER_ERROR\", \"message\": \"Erro ao confirmar carteira: ...\" }"
                            )
                    )
            )
    })
    public ResponseEntity<?> confirmarEscolhaCarteira(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails,
            @RequestBody @Valid EscolhaCarteiraDTO dto) {

        log.info("[POST] /api/carteiras/selecionar-carteira - usuario={} tipo={}",
                userDetails.getUsername(), dto.getTipoCarteira());
        try {
            Usuario u = usuarioService.findByEmail(userDetails.getUsername());
            ObjetivoUsuario obj = objetivoService
                    .buscarUltimoPorUsuario(u)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Objetivo não encontrado"));

            String tipo = dto.getTipoCarteira() == 0 ? "conservadora" : "agressiva";
            List<CarteiraPercentual> lista = carteiraPercentualRepository
                    .findByUsuarioIdAndObjetivoId(u.getId(), obj.getId())
                    .stream()
                    .filter(p -> p.getTipoCarteira().equalsIgnoreCase(tipo))
                    .collect(Collectors.toList());

            JSONObject dist = new JSONObject();
            lista.forEach(cp -> dist.put(cp.getSegmento(), cp.getPercentual()));

            JSONObject respostaIA = iaService.chamarOpenAI(
                    iaService.gerarPromptAtivos(obj, new JSONObject(Map.of(tipo, dist)), tipo)
            );
            JSONObject carteiraJson = respostaIA.getJSONObject("carteira");

            carteiraAtivoService.salvarAtivos(tipo, carteiraJson, u, obj, new JSONObject(Map.of(tipo, dist)));

            CarteiraUsuario cu = carteiraService.buscarPorObjetivo(obj).orElse(new CarteiraUsuario());
            cu.setUsuario(u);
            cu.setObjetivoUsuario(obj);
            cu.setCarteiraSelecionada(tipo);
            if (dto.getTipoCarteira() == 0) {
                cu.setCarteiraConservadoraJson(carteiraJson.toString());
            } else {
                cu.setCarteiraAgressivaJson(carteiraJson.toString());
            }
            carteiraService.salvar(cu);

            log.info("Carteira '{}' salva para usuario={}", tipo, u.getId());
            return ResponseEntity.ok(Map.of(
                    "message", "Carteira confirmada com sucesso",
                    "carteiraEscolhida", tipo,
                    "carteira", carteiraJson.toMap()
            ));

        } catch (ResponseStatusException ex) {
            log.warn("selecionar-carteira - {}", ex.getReason());
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", ex.getStatusCode().value(),
                    "error",  ex.getStatusCode().toString(),
                    "message", ex.getReason()
            ));
        } catch (Exception ex) {
            log.error("Erro interno em selecionar-carteira", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error",  HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    "message", "Erro ao confirmar carteira: " + ex.getMessage()
            ));
        }
    }

    /**
     * Busca a carteira já confirmada pelo usuário (conservadora ou agressiva).
     *
     * @param userDetails detalhes do usuário (JWT)
     * @return 200 + objeto \"carteira\", ou 404/500 em erro
     */
    @GetMapping("/selecionada")
    @Operation(
            summary = "Visualizar carteira confirmada",
            description = "Retorna a carteira previamente confirmada pelo usuário.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Carteira retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"carteira\": {\n" +
                                            "    \"fiis\": [\"HGLG11\"],\n" +
                                            "    \"cripto\": [],\n" +
                                            "    \"rendaFixa\": [\"LCA 2028\"],\n" +
                                            "    \"acoes\": [\"ITUB4\"]\n" +
                                            "  }\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhuma carteira confirmada encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{ \"status\": \"404\", \"error\": \"404 NOT_FOUND\", \"message\": \"Nenhuma carteira confirmada para este objetivo\" }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno ao recuperar carteira",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{ \"status\": \"500\", \"error\": \"500 INTERNAL_SERVER_ERROR\", \"message\": \"...\" }"
                            )
                    )
            )
    })
    public ResponseEntity<?> visualizarCarteiraSelecionada(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        log.info("[GET] /api/carteiras/selecionada - usuário={}", userDetails.getUsername());
        try {
            Usuario u = usuarioService.findByEmail(userDetails.getUsername());
            ObjetivoUsuario obj = objetivoService.buscarUltimoPorUsuario(u)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Nenhum objetivo encontrado para o usuário"));

            CarteiraUsuario cu = carteiraService.buscarPorObjetivo(obj)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Nenhuma carteira confirmada para este objetivo"));

            String tipo = cu.getCarteiraSelecionada();
            String raw = "conservadora".equalsIgnoreCase(tipo)
                    ? cu.getCarteiraConservadoraJson()
                    : cu.getCarteiraAgressivaJson();
            JSONObject json = new JSONObject(raw);

            log.info("Carteira '{}' retornada para usuario={}", tipo, u.getId());
            return ResponseEntity.ok(Map.of("carteira", json.toMap()));

        } catch (ResponseStatusException ex) {
            log.warn("visualizar-carteira - {}", ex.getReason());
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "status", ex.getStatusCode().value(),
                    "error",  ex.getStatusCode().toString(),
                    "message", ex.getReason()
            ));
        } catch (Exception ex) {
            log.error("Erro interno em visualizar-carteira", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error",  HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    "message", "Erro ao recuperar carteira: " + ex.getMessage()
            ));
        }
    }



    /**
     * DTO para receber a escolha de tipo de carteira pelo usuário.
     * <p>0 = conservadora, 1 = agressiva</p>
     */
    @Data
    public static class EscolhaCarteiraDTO {
        private int tipoCarteira;
    }
}
