package br.csi.rep_hub.controller;

import br.csi.rep_hub.dto.extratos.*;
import br.csi.rep_hub.model.extrato.TipoPeriodoExtrato;
import br.csi.rep_hub.service.ExtratoFinanceiroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/extrato")
@Tag(name = "Extratos e indicadores financeiros")
@SecurityRequirement(name = "bearerAuth")
public class ExtratoFinanceiroController {
    private final ExtratoFinanceiroService extratos;

    public ExtratoFinanceiroController(ExtratoFinanceiroService extratos) {
        this.extratos = extratos;
    }

    @GetMapping("/individual/republica/{republicaId}")
    @Operation(summary = "Consultar o próprio extrato individual",
            description = "Sem participação informada, reúne todas as participações do titular " +
                    "na mesma república. Ex-moradores mantêm acesso enquanto a república estiver ativa.")
    public ExtratoIndividualResposta individual(
            @PathVariable Long republicaId,
            @RequestParam TipoPeriodoExtrato periodo,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth inicio,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth fim,
            @RequestParam(required = false) Integer ano,
            @Parameter(description = "Pode ser repetido para reunir vínculos do próprio titular")
            @RequestParam(required = false) List<UUID> participacaoUuid) {
        return extratos.individual(republicaId, participacaoUuid,
                new FiltroExtratoDados(periodo, mes, inicio, fim, ano));
    }

    @GetMapping("/coletivo/republica/{republicaId}")
    @Operation(summary = "Consultar o extrato coletivo",
            description = "Disponível somente a participantes ativos da república")
    public ExtratoColetivoResposta coletivo(
            @PathVariable Long republicaId,
            @RequestParam TipoPeriodoExtrato periodo,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth inicio,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth fim,
            @RequestParam(required = false) Integer ano) {
        return extratos.coletivo(republicaId,
                new FiltroExtratoDados(periodo, mes, inicio, fim, ano));
    }
}
