package br.csi.rep_hub.controller;

import br.csi.rep_hub.dto.tarefas.AtribuicaoTarefaDados;
import br.csi.rep_hub.dto.tarefas.AtribuicaoTarefaResposta;
import br.csi.rep_hub.service.AtribuicaoTarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/atribuicao-tarefa")
@Tag(name = "Atribuição de tarefa")
@SecurityRequirement(name = "bearerAuth")
public class AtribuicaoTarefaController {
    private final AtribuicaoTarefaService atribuicoes;

    public AtribuicaoTarefaController(AtribuicaoTarefaService atribuicoes) {
        this.atribuicoes = atribuicoes;
    }

    @PostMapping("/tarefa/{tarefaId}")
    @Operation(summary = "Atribuir responsável",
            description = "Atribui um participante ativo a uma tarefa pendente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Responsável atribuído",
                    content = @Content(schema = @Schema(implementation = AtribuicaoTarefaResposta.class))),
            @ApiResponse(responseCode = "400", description = "Participação de outra república"),
            @ApiResponse(responseCode = "403", description = "Participação ativa necessária"),
            @ApiResponse(responseCode = "404", description = "Tarefa ou participação não encontrada"),
            @ApiResponse(responseCode = "409", description = "Tarefa concluída ou atribuição duplicada")
    })
    public ResponseEntity<AtribuicaoTarefaResposta> atribuir(
            @PathVariable Long tarefaId,
            @RequestBody @Valid AtribuicaoTarefaDados dados,
            UriComponentsBuilder uriBuilder) {
        AtribuicaoTarefaResposta atribuicao = atribuicoes.atribuir(tarefaId, dados);
        URI uri = uriBuilder.path("/atribuicao-tarefa/uuid/{uuid}")
                .buildAndExpand(atribuicao.uuid()).toUri();
        return ResponseEntity.created(uri).body(atribuicao);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar atribuição por ID")
    public AtribuicaoTarefaResposta porId(@PathVariable Long id) {
        return atribuicoes.porId(id);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Consultar atribuição por UUID")
    public AtribuicaoTarefaResposta porUuid(@PathVariable UUID uuid) {
        return atribuicoes.porUuid(uuid);
    }

    @PutMapping("/{id}/concluir")
    @Operation(summary = "Concluir atribuição por ID",
            description = "Somente o responsável ativo pode marcar sua conclusão")
    public AtribuicaoTarefaResposta concluir(@PathVariable Long id) {
        return atribuicoes.concluir(id);
    }

    @PutMapping("/uuid/{uuid}/concluir")
    @Operation(summary = "Concluir atribuição por UUID",
            description = "Somente o responsável ativo pode marcar sua conclusão")
    public AtribuicaoTarefaResposta concluirUuid(@PathVariable UUID uuid) {
        return atribuicoes.concluir(uuid);
    }

    @PutMapping("/{id}/reabrir")
    @Operation(summary = "Reabrir atribuição por ID",
            description = "Somente o responsável ativo pode desfazer sua conclusão")
    public AtribuicaoTarefaResposta reabrir(@PathVariable Long id) {
        return atribuicoes.reabrir(id);
    }

    @PutMapping("/uuid/{uuid}/reabrir")
    @Operation(summary = "Reabrir atribuição por UUID",
            description = "Somente o responsável ativo pode desfazer sua conclusão")
    public AtribuicaoTarefaResposta reabrirUuid(@PathVariable UUID uuid) {
        return atribuicoes.reabrir(uuid);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar atribuição por ID",
            description = "Exige responsabilidade, autoria da atribuição ou administração")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        atribuicoes.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/uuid/{uuid}")
    @Operation(summary = "Cancelar atribuição por UUID",
            description = "Exige responsabilidade, autoria da atribuição ou administração")
    public ResponseEntity<Void> cancelarUuid(@PathVariable UUID uuid) {
        atribuicoes.cancelar(uuid);
        return ResponseEntity.noContent().build();
    }
}
