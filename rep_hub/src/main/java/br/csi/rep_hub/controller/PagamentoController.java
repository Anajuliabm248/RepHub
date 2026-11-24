package br.csi.rep_hub.controller;

import br.csi.rep_hub.dto.pagamentos.CancelamentoPagamentoDados;
import br.csi.rep_hub.dto.pagamentos.PagamentoDados;
import br.csi.rep_hub.dto.pagamentos.PagamentoResposta;
import br.csi.rep_hub.service.PagamentoService;
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
@RequestMapping("/pagamento")
@Tag(name = "Pagamentos")
@SecurityRequirement(name = "bearerAuth")
public class PagamentoController {
    private final PagamentoService pagamentos;

    public PagamentoController(PagamentoService pagamentos) {
        this.pagamentos = pagamentos;
    }

    @PostMapping("/parcela/{parcelaId}")
    @Operation(summary = "Registrar pagamento",
            description = "O devedor registra um pagamento inicialmente pendente")
    public ResponseEntity<PagamentoResposta> cadastrar(
            @PathVariable Long parcelaId, @RequestBody @Valid PagamentoDados dados,
            UriComponentsBuilder uriBuilder) {
        PagamentoResposta resposta = pagamentos.cadastrar(parcelaId, dados);
        URI uri = uriBuilder.path("/pagamento/uuid/{uuid}")
                .buildAndExpand(resposta.uuid()).toUri();
        return ResponseEntity.created(uri).body(resposta);
    }

    @PostMapping("/parcela/uuid/{parcelaUuid}")
    @Operation(summary = "Registrar pagamento por UUID da parcela")
    public ResponseEntity<PagamentoResposta> cadastrarUuid(
            @PathVariable UUID parcelaUuid, @RequestBody @Valid PagamentoDados dados,
            UriComponentsBuilder uriBuilder) {
        PagamentoResposta resposta = pagamentos.cadastrar(parcelaUuid, dados);
        URI uri = uriBuilder.path("/pagamento/uuid/{uuid}")
                .buildAndExpand(resposta.uuid()).toUri();
        return ResponseEntity.created(uri).body(resposta);
    }

    @GetMapping("/parcela/{parcelaId}")
    @Operation(summary = "Listar pagamentos de uma parcela")
    public List<PagamentoResposta> listarDaParcela(@PathVariable Long parcelaId) {
        return pagamentos.listarDaParcela(parcelaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar pagamento por ID")
    public PagamentoResposta porId(@PathVariable Long id) {
        return pagamentos.porId(id);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Consultar pagamento por UUID")
    public PagamentoResposta porUuid(@PathVariable UUID uuid) {
        return pagamentos.porUuid(uuid);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar pagamento pendente por ID")
    public PagamentoResposta editar(@PathVariable Long id,
                                     @RequestBody @Valid PagamentoDados dados) {
        return pagamentos.editar(id, dados);
    }

    @PutMapping("/uuid/{uuid}")
    @Operation(summary = "Editar pagamento pendente por UUID")
    public PagamentoResposta editarUuid(@PathVariable UUID uuid,
                                         @RequestBody @Valid PagamentoDados dados) {
        return pagamentos.editar(uuid, dados);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar pagamento pendente por ID")
    public ResponseEntity<Void> cancelar(@PathVariable Long id,
                                          @RequestBody @Valid CancelamentoPagamentoDados dados) {
        pagamentos.cancelar(id, dados);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/uuid/{uuid}")
    @Operation(summary = "Cancelar pagamento pendente por UUID")
    public ResponseEntity<Void> cancelarUuid(
            @PathVariable UUID uuid,
            @RequestBody @Valid CancelamentoPagamentoDados dados) {
        pagamentos.cancelar(uuid, dados);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar pagamento por ID",
            description = "Exige administrador ativo e revalida o saldo sob bloqueio")
    public PagamentoResposta confirmar(@PathVariable Long id) {
        return pagamentos.confirmar(id);
    }

    @PatchMapping("/uuid/{uuid}/confirmar")
    @Operation(summary = "Confirmar pagamento por UUID")
    public PagamentoResposta confirmarUuid(@PathVariable UUID uuid) {
        return pagamentos.confirmar(uuid);
    }
}
