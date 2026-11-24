package br.csi.rep_hub.controller;

import br.csi.rep_hub.dto.despesas.DespesaDados;
import br.csi.rep_hub.dto.despesas.DespesaResposta;
import br.csi.rep_hub.dto.despesas.ParcelaDespesaResposta;
import br.csi.rep_hub.service.DespesaService;
import br.csi.rep_hub.service.ParcelaDespesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/despesa")
@Tag(name = "Despesas e parcelas")
@SecurityRequirement(name = "bearerAuth")
public class DespesaController {
    private final DespesaService despesas;
    private final ParcelaDespesaService parcelas;

    public DespesaController(DespesaService despesas, ParcelaDespesaService parcelas) {
        this.despesas = despesas;
        this.parcelas = parcelas;
    }

    @PostMapping("/republica/{republicaId}")
    @Operation(summary = "Cadastrar despesa",
            description = "Cria a despesa e seu rateio na mesma transação")
    public ResponseEntity<DespesaResposta> cadastrar(
            @PathVariable Long republicaId, @RequestBody @Valid DespesaDados dados,
            UriComponentsBuilder uriBuilder) {
        DespesaResposta resposta = despesas.cadastrar(republicaId, dados);
        URI uri = uriBuilder.path("/despesa/uuid/{uuid}")
                .buildAndExpand(resposta.uuid()).toUri();
        return ResponseEntity.created(uri).body(resposta);
    }

    @GetMapping("/republica/{republicaId}")
    @Operation(summary = "Listar despesas da república")
    public List<DespesaResposta> listar(@PathVariable Long republicaId) {
        return despesas.listar(republicaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar despesa por ID")
    public DespesaResposta porId(@PathVariable Long id) {
        return despesas.porId(id);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Consultar despesa por UUID")
    public DespesaResposta porUuid(@PathVariable UUID uuid) {
        return despesas.porUuid(uuid);
    }

    @GetMapping("/parcela/{id}")
    @Operation(summary = "Consultar parcela por ID")
    public ParcelaDespesaResposta parcelaPorId(@PathVariable Long id) {
        return parcelas.porId(id);
    }

    @GetMapping("/parcela/uuid/{uuid}")
    @Operation(summary = "Consultar parcela por UUID")
    public ParcelaDespesaResposta parcelaPorUuid(@PathVariable UUID uuid) {
        return parcelas.porUuid(uuid);
    }

    @GetMapping("/participacao/{participacaoId}/parcelas")
    @Operation(summary = "Listar parcelas de uma participação",
            description = "Mantém o acesso do ex-morador ao próprio financeiro")
    public List<ParcelaDespesaResposta> parcelasDaParticipacao(
            @PathVariable Long participacaoId) {
        return parcelas.listarDaParticipacao(participacaoId);
    }

    @GetMapping("/participacao/uuid/{participacaoUuid}/parcelas")
    @Operation(summary = "Listar parcelas por UUID da participação")
    public List<ParcelaDespesaResposta> parcelasDaParticipacaoUuid(
            @PathVariable UUID participacaoUuid) {
        return parcelas.listarDaParticipacao(participacaoUuid);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Alterar despesa por ID",
            description = "Exige autoria ativa ou administração e respeita pagamentos existentes")
    public DespesaResposta alterar(@PathVariable Long id,
                                    @RequestBody @Valid DespesaDados dados) {
        return despesas.alterar(id, dados);
    }

    @PutMapping("/uuid/{uuid}")
    @Operation(summary = "Alterar despesa por UUID")
    public DespesaResposta alterarUuid(@PathVariable UUID uuid,
                                        @RequestBody @Valid DespesaDados dados) {
        return despesas.alterar(uuid, dados);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar despesa por ID")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        despesas.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/uuid/{uuid}")
    @Operation(summary = "Cancelar despesa por UUID")
    public ResponseEntity<Void> cancelarUuid(@PathVariable UUID uuid) {
        despesas.cancelar(uuid);
        return ResponseEntity.noContent().build();
    }
}
