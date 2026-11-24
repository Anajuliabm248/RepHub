package br.csi.rep_hub.controller;

import br.csi.rep_hub.dto.convite.ConsumirConviteDados;
import br.csi.rep_hub.dto.convite.ConviteResposta;
import br.csi.rep_hub.dto.participacao.ParticipacaoResposta;
import br.csi.rep_hub.service.ConviteService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/convite")
@Tag(name = "Convite")
@SecurityRequirement(name = "bearerAuth")
public class ConviteController {
    private final ConviteService convites;

    public ConviteController(ConviteService convites) {
        this.convites = convites;
    }

    @PostMapping("/republica/{republicaId}")
    @Operation(summary = "Emitir convite",
            description = "Emite um convite de uso único, válido por 48 horas. Exige administração")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Convite emitido",
                    content = @Content(schema = @Schema(implementation = ConviteResposta.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Permissão de administrador necessária"),
            @ApiResponse(responseCode = "404", description = "República não encontrada")
    })
    public ResponseEntity<ConviteResposta> emitir(@PathVariable Long republicaId,
                                                   UriComponentsBuilder uriBuilder) {
        ConviteResposta convite = convites.emitir(republicaId);
        URI uri = uriBuilder.path("/convite/uuid/{uuid}").buildAndExpand(convite.uuid()).toUri();
        return ResponseEntity.created(uri).body(convite);
    }

    @GetMapping("/republica/{republicaId}")
    @Operation(summary = "Listar convites",
            description = "Lista os convites da república, inclusive utilizados, cancelados e expirados")
    public List<ConviteResposta> listar(@PathVariable Long republicaId) {
        return convites.listar(republicaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar convite por ID",
            description = "Consulta um convite da república administrada pelo usuário")
    public ConviteResposta porId(@PathVariable Long id) {
        return convites.porId(id);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Consultar convite por UUID",
            description = "Consulta um convite da república administrada pelo usuário")
    public ConviteResposta porUuid(@PathVariable UUID uuid) {
        return convites.porUuid(uuid);
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Consultar convite por código",
            description = "Permite que uma conta ativa confira o convite antes de utilizá-lo")
    public ConviteResposta porCodigo(@PathVariable String codigo) {
        return convites.porCodigo(codigo);
    }

    @PostMapping("/consumir")
    @Operation(summary = "Consumir convite",
            description = "Utiliza o convite e cria uma participação de morador na mesma transação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convite utilizado e participação criada",
                    content = @Content(schema = @Schema(implementation = ParticipacaoResposta.class))),
            @ApiResponse(responseCode = "400", description = "Código não informado"),
            @ApiResponse(responseCode = "404", description = "Convite não encontrado"),
            @ApiResponse(responseCode = "409",
                    description = "Convite indisponível ou usuário já possui participação ativa")
    })
    public ParticipacaoResposta consumir(@RequestBody @Valid ConsumirConviteDados dados) {
        return convites.consumir(dados.codigo());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar convite por ID",
            description = "Cancela um convite disponível e não expirado. Exige administração")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        convites.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/uuid/{uuid}")
    @Operation(summary = "Cancelar convite por UUID",
            description = "Cancela um convite disponível e não expirado. Exige administração")
    public ResponseEntity<Void> cancelarUuid(@PathVariable UUID uuid) {
        convites.cancelar(uuid);
        return ResponseEntity.noContent().build();
    }
}
