package br.csi.rep_hub.controller;

import br.csi.rep_hub.controller.dto.RepublicaDados;
import br.csi.rep_hub.controller.dto.RepublicaResposta;
import br.csi.rep_hub.service.RepublicaService;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/republica")
@Tag(name = "República")
public class RepublicaController {
    private final RepublicaService republicas;

    public RepublicaController(RepublicaService republicas) {
        this.republicas = republicas;
    }

    @GetMapping("/listar")
    @Operation(summary = "Listar minhas repúblicas", description = "Retorna as repúblicas ativas das quais o usuário participa",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada, possivelmente vazia",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RepublicaResposta.class)))),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public List<RepublicaResposta> listar() {
        return republicas.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar república por ID", description = "Consulta uma república da qual o usuário participa",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "República retornada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RepublicaResposta.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Participação ativa necessária"),
            @ApiResponse(responseCode = "404", description = "República não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public RepublicaResposta porId(@PathVariable Long id) {
        return republicas.porId(id);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Consultar república por UUID", description = "Consulta uma república da qual o usuário participa",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "República retornada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RepublicaResposta.class))),
            @ApiResponse(responseCode = "400", description = "UUID inválido"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Participação ativa necessária"),
            @ApiResponse(responseCode = "404", description = "República não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public RepublicaResposta porUuid(@PathVariable UUID uuid) {
        return republicas.porUuid(uuid);
    }

    @PostMapping
    @Operation(summary = "Criar república", description = "Cria uma república e a participação do fundador como administrador",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "República criada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RepublicaResposta.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "409", description = "Usuário já participa de uma república ativa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<RepublicaResposta> cadastrar(@RequestBody @Valid RepublicaDados dados,
                                                         UriComponentsBuilder uriBuilder) {
        RepublicaResposta republica = republicas.cadastrar(dados);
        URI uri = uriBuilder.path("/republica/uuid/{uuid}").buildAndExpand(republica.uuid()).toUri();
        return ResponseEntity.created(uri).body(republica);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar república por ID", description = "Atualiza os dados de uma república administrada pelo usuário",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "República atualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RepublicaResposta.class))),
            @ApiResponse(responseCode = "400", description = "Dados ou ID inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Permissão de administrador necessária"),
            @ApiResponse(responseCode = "404", description = "República não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public RepublicaResposta atualizar(@PathVariable Long id, @RequestBody @Valid RepublicaDados dados) {
        return republicas.atualizar(id, dados);
    }

    @PutMapping("/uuid/{uuid}")
    @Operation(summary = "Atualizar república por UUID", description = "Atualiza os dados de uma república administrada pelo usuário",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "República atualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RepublicaResposta.class))),
            @ApiResponse(responseCode = "400", description = "Dados ou UUID inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Permissão de administrador necessária"),
            @ApiResponse(responseCode = "404", description = "República não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public RepublicaResposta atualizarUuid(@PathVariable UUID uuid, @RequestBody @Valid RepublicaDados dados) {
        return republicas.atualizar(uuid, dados);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Encerrar república", description = "Desativa a república e encerra suas participações ativas",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "República encerrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Permissão de administrador necessária"),
            @ApiResponse(responseCode = "404", description = "República não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> encerrar(@PathVariable Long id) {
        republicas.encerrar(id);
        return ResponseEntity.noContent().build();
    }
}
