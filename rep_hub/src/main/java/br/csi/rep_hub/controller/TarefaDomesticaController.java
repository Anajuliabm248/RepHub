package br.csi.rep_hub.controller;

import br.csi.rep_hub.dto.TarefaDados;
import br.csi.rep_hub.dto.TarefaResposta;
import br.csi.rep_hub.service.TarefaDomesticaService;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tarefa")
@Tag(name = "Tarefa doméstica")
@SecurityRequirement(name = "bearerAuth")
public class TarefaDomesticaController {
    private final TarefaDomesticaService tarefas;

    public TarefaDomesticaController(TarefaDomesticaService tarefas) {
        this.tarefas = tarefas;
    }

    @PostMapping("/republica/{republicaId}")
    @Operation(summary = "Criar tarefa",
            description = "Cria uma tarefa sem responsável. Qualquer participante ativo pode criar")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa criada",
                    content = @Content(schema = @Schema(implementation = TarefaResposta.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Participação ativa necessária"),
            @ApiResponse(responseCode = "404", description = "República não encontrada")
    })
    public ResponseEntity<TarefaResposta> cadastrar(@PathVariable Long republicaId,
                                                     @RequestBody @Valid TarefaDados dados,
                                                     UriComponentsBuilder uriBuilder) {
        TarefaResposta tarefa = tarefas.cadastrar(republicaId, dados);
        URI uri = uriBuilder.path("/tarefa/uuid/{uuid}").buildAndExpand(tarefa.uuid()).toUri();
        return ResponseEntity.created(uri).body(tarefa);
    }

    @GetMapping("/republica/{republicaId}")
    @Operation(summary = "Listar tarefas em andamento",
            description = "Lista tarefas pendentes e concluídas; tarefas canceladas ficam fora do quadro")
    public List<TarefaResposta> listar(@PathVariable Long republicaId) {
        return tarefas.listar(republicaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar tarefa por ID",
            description = "Consulta a tarefa e o histórico de suas atribuições")
    public TarefaResposta porId(@PathVariable Long id) {
        return tarefas.porId(id);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Consultar tarefa por UUID",
            description = "Consulta a tarefa e o histórico de suas atribuições")
    public TarefaResposta porUuid(@PathVariable UUID uuid) {
        return tarefas.porUuid(uuid);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar tarefa por ID",
            description = "Edita uma tarefa pendente. Exige autoria ou administração")
    public TarefaResposta atualizar(@PathVariable Long id,
                                     @RequestBody @Valid TarefaDados dados) {
        return tarefas.atualizar(id, dados);
    }

    @PutMapping("/uuid/{uuid}")
    @Operation(summary = "Editar tarefa por UUID",
            description = "Edita uma tarefa pendente. Exige autoria ou administração")
    public TarefaResposta atualizarUuid(@PathVariable UUID uuid,
                                         @RequestBody @Valid TarefaDados dados) {
        return tarefas.atualizar(uuid, dados);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar tarefa por ID",
            description = "Cancela logicamente a tarefa e suas atribuições pendentes")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        tarefas.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/uuid/{uuid}")
    @Operation(summary = "Cancelar tarefa por UUID",
            description = "Cancela logicamente a tarefa e suas atribuições pendentes")
    public ResponseEntity<Void> cancelarUuid(@PathVariable UUID uuid) {
        tarefas.cancelar(uuid);
        return ResponseEntity.noContent().build();
    }
}
