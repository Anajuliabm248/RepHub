package br.csi.rep_hub.controller;

import br.csi.rep_hub.controller.dto.UsuarioAtualizacao;
import br.csi.rep_hub.controller.dto.UsuarioCadastro;
import br.csi.rep_hub.controller.dto.UsuarioResposta;
import br.csi.rep_hub.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.UUID;

@RestController
@RequestMapping("/usuario")
@Tag(name = "Usuário")
public class UsuarioController {
    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @PostMapping
    @Operation(summary = "Cadastrar usuário", description = "Cria uma conta ativa com senha armazenada como hash")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResposta.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<UsuarioResposta> cadastrar(@RequestBody @Valid UsuarioCadastro dados,
                                                       UriComponentsBuilder uriBuilder) {
        UsuarioResposta usuario = usuarios.cadastrar(dados);
        URI uri = uriBuilder.path("/usuario/uuid/{uuid}").buildAndExpand(usuario.uuid()).toUri();
        return ResponseEntity.created(uri).body(usuario);
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar minha conta", description = "Retorna os dados da conta autenticada",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta retornada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResposta.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public UsuarioResposta atual() {
        return usuarios.atual();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar usuário por ID", description = "Consulta a própria conta pelo ID interno",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário retornado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResposta.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Conta de outro usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public UsuarioResposta porId(@PathVariable Long id) {
        return usuarios.porId(id);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Consultar usuário por UUID", description = "Consulta a própria conta pelo UUID",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário retornado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResposta.class))),
            @ApiResponse(responseCode = "400", description = "UUID inválido"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Conta de outro usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public UsuarioResposta porUuid(@PathVariable UUID uuid) {
        return usuarios.porUuid(uuid);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário por ID", description = "Atualiza os dados da própria conta",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResposta.class))),
            @ApiResponse(responseCode = "400", description = "Dados ou ID inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Conta de outro usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public UsuarioResposta atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioAtualizacao dados) {
        return usuarios.atualizar(id, dados);
    }

    @PutMapping("/uuid/{uuid}")
    @Operation(summary = "Atualizar usuário por UUID", description = "Atualiza os dados da própria conta",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResposta.class))),
            @ApiResponse(responseCode = "400", description = "Dados ou UUID inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Conta de outro usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public UsuarioResposta atualizarUuid(@PathVariable UUID uuid, @RequestBody @Valid UsuarioAtualizacao dados) {
        return usuarios.atualizar(uuid, dados);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar usuário por ID", description = "Desativa a própria conta sem excluir o registro",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta desativada", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Conta de outro usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conta é do último administrador ativo"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        usuarios.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/uuid/{uuid}")
    @Operation(summary = "Desativar usuário por UUID", description = "Desativa a própria conta sem excluir o registro",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta desativada", content = @Content),
            @ApiResponse(responseCode = "400", description = "UUID inválido"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Conta de outro usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conta é do último administrador ativo"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> desativarUuid(@PathVariable UUID uuid) {
        usuarios.desativar(uuid);
        return ResponseEntity.noContent().build();
    }
}
