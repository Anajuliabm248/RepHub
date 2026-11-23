package br.csi.rep_hub.controller;

import br.csi.rep_hub.dto.AvisoDados;
import br.csi.rep_hub.dto.AvisoResposta;
import br.csi.rep_hub.service.AvisoService;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/aviso")
@Tag(name = "Quadro de avisos")
@SecurityRequirement(name = "bearerAuth")
public class AvisoController {
    private final AvisoService avisos;

    public AvisoController(AvisoService avisos) {
        this.avisos = avisos;
    }

    @PostMapping("/republica/{republicaId}")
    @Operation(summary = "Publicar aviso",
            description = "Publica um aviso no quadro. Exige participação ativa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aviso publicado",
                    content = @Content(schema = @Schema(implementation = AvisoResposta.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Participação ativa necessária"),
            @ApiResponse(responseCode = "404", description = "República não encontrada")
    })
    public ResponseEntity<AvisoResposta> publicar(@PathVariable Long republicaId,
                                                   @RequestBody @Valid AvisoDados dados,
                                                   UriComponentsBuilder uriBuilder) {
        AvisoResposta aviso = avisos.publicar(republicaId, dados);
        URI uri = uriBuilder.path("/aviso/uuid/{uuid}").buildAndExpand(aviso.uuid()).toUri();
        return ResponseEntity.created(uri).body(aviso);
    }

    @GetMapping("/republica/{republicaId}")
    @Operation(summary = "Listar avisos",
            description = "Lista somente os avisos visíveis do quadro da república")
    public List<AvisoResposta> listar(@PathVariable Long republicaId) {
        return avisos.listar(republicaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar aviso por ID",
            description = "Consulta um aviso visível da república")
    public AvisoResposta porId(@PathVariable Long id) {
        return avisos.porId(id);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Consultar aviso por UUID",
            description = "Consulta um aviso visível da república")
    public AvisoResposta porUuid(@PathVariable UUID uuid) {
        return avisos.porUuid(uuid);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar aviso por ID",
            description = "Edita um aviso visível. Ação exclusiva do autor ativo")
    public AvisoResposta editar(@PathVariable Long id,
                                 @RequestBody @Valid AvisoDados dados) {
        return avisos.editar(id, dados);
    }

    @PutMapping("/uuid/{uuid}")
    @Operation(summary = "Editar aviso por UUID",
            description = "Edita um aviso visível. Ação exclusiva do autor ativo")
    public AvisoResposta editarUuid(@PathVariable UUID uuid,
                                     @RequestBody @Valid AvisoDados dados) {
        return avisos.editar(uuid, dados);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ocultar aviso por ID",
            description = "Oculta o aviso sem apagar seu conteúdo. Exige autoria ou administração")
    public ResponseEntity<Void> ocultar(@PathVariable Long id) {
        avisos.ocultar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/uuid/{uuid}")
    @Operation(summary = "Ocultar aviso por UUID",
            description = "Oculta o aviso sem apagar seu conteúdo. Exige autoria ou administração")
    public ResponseEntity<Void> ocultarUuid(@PathVariable UUID uuid) {
        avisos.ocultar(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restaurar")
    @Operation(summary = "Restaurar aviso por ID",
            description = "Restaura um aviso oculto. Exige autoria ou administração")
    public AvisoResposta restaurar(@PathVariable Long id) {
        return avisos.restaurar(id);
    }

    @PatchMapping("/uuid/{uuid}/restaurar")
    @Operation(summary = "Restaurar aviso por UUID",
            description = "Restaura um aviso oculto. Exige autoria ou administração")
    public AvisoResposta restaurarUuid(@PathVariable UUID uuid) {
        return avisos.restaurar(uuid);
    }
}
