package br.csi.rep_hub.controller;

import br.csi.rep_hub.controller.dto.ParticipacaoPapel;
import br.csi.rep_hub.controller.dto.ParticipacaoResposta;
import br.csi.rep_hub.service.ParticipacaoRepublicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/participacao")
@Tag(name = "Participação de república")
public class ParticipacaoRepublicaController {
    private final ParticipacaoRepublicaService participacoes;

    public ParticipacaoRepublicaController(ParticipacaoRepublicaService participacoes) {
        this.participacoes = participacoes;
    }

    @GetMapping("/listar")
    @Operation(summary = "Listar participações", description = "Retorna as participações ativas da república do usuário",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada, possivelmente vazia",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ParticipacaoResposta.class)))),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public List<ParticipacaoResposta> listar() {
        return participacoes.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar participação por ID", description = "Consulta uma participação da própria república",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participação retornada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParticipacaoResposta.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Participação ativa necessária"),
            @ApiResponse(responseCode = "404", description = "Participação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ParticipacaoResposta porId(@PathVariable Long id) {
        return participacoes.porId(id);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Consultar participação por UUID", description = "Consulta uma participação da própria república",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participação retornada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParticipacaoResposta.class))),
            @ApiResponse(responseCode = "400", description = "UUID inválido"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Participação ativa necessária"),
            @ApiResponse(responseCode = "404", description = "Participação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ParticipacaoResposta porUuid(@PathVariable UUID uuid) {
        return participacoes.porUuid(uuid);
    }

    @PutMapping("/{id}/papel")
    @Operation(summary = "Alterar papel por ID", description = "Altera o papel de um participante ativo; exige administração",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Papel alterado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParticipacaoResposta.class))),
            @ApiResponse(responseCode = "400", description = "Dados ou ID inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Permissão de administrador necessária"),
            @ApiResponse(responseCode = "404", description = "Participação não encontrada"),
            @ApiResponse(responseCode = "409", description = "Participação encerrada ou último administrador"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ParticipacaoResposta alterarPapel(@PathVariable Long id, @RequestBody @Valid ParticipacaoPapel dados) {
        return participacoes.alterarPapel(id, dados);
    }

    @PutMapping("/uuid/{uuid}/papel")
    @Operation(summary = "Alterar papel por UUID", description = "Altera o papel de um participante ativo; exige administração",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Papel alterado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParticipacaoResposta.class))),
            @ApiResponse(responseCode = "400", description = "Dados ou UUID inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Permissão de administrador necessária"),
            @ApiResponse(responseCode = "404", description = "Participação não encontrada"),
            @ApiResponse(responseCode = "409", description = "Participação encerrada ou último administrador"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ParticipacaoResposta alterarPapelUuid(@PathVariable UUID uuid,
                                                 @RequestBody @Valid ParticipacaoPapel dados) {
        return participacoes.alterarPapel(uuid, dados);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Encerrar participação", description = "Registra a saída do titular ou a remoção por administrador",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Participação encerrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito ao titular ou administrador"),
            @ApiResponse(responseCode = "404", description = "Participação não encontrada"),
            @ApiResponse(responseCode = "409", description = "Participação já encerrada ou último administrador"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> encerrar(@PathVariable Long id) {
        participacoes.encerrar(id);
        return ResponseEntity.noContent().build();
    }
}
