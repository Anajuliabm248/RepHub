package br.csi.rep_hub.controller;

import br.csi.rep_hub.model.usuario.Usuario;
import br.csi.rep_hub.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
@Tag(name = "Usuario", description = "Endpoints relacionados a usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/listar")
    @Operation(summary = "Listar usuários", description = "Retorna uma lista de todos os usuários cadastrados")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários retornada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Nenhum usuário encontrado")
    @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor")
    public List<Usuario> listar(){
        return this.usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter usuário", description = "Retorna os detalhes de um usuário específico")
    @ApiResponses(value = {
        @ApiResponse(
                        responseCode = "200",
                        description = "Usuário retornado com sucesso",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Nenhum usuário encontrado"),
        @ApiResponse(
                responseCode = "500",
                description = "Erro interno do servidor")
    })
    public Usuario usuario(@PathVariable Long id){
        return this.usuarioService.getUsuario(id);
    }

    @PostMapping
    @Operation(
            summary = "Criar um novo Usuário",
            description = "Salva um novo usuário no banco de dados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário criado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public void salvar(@RequestBody Usuario usuario) {
        this.usuarioService.salvar(usuario);
    }

    @PutMapping
    @Operation(
            summary = "Atualizar um usuário existente",
            description = "Atualiza os dados de um usuário cadastrado no banco de dados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public void atualizar(@RequestBody Usuario usuario) {
        this.usuarioService.atualizar(usuario);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir usuário",
            description = "Remove um usuário do banco de dados utilizando seu ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public void deletar(@PathVariable Long id) {
        this.usuarioService.excluir(id);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(
            summary = "Buscar usuário por UUID",
            description = "Busca um usuário cadastrado utilizando seu UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Usuario.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public Usuario usuarioUUID(@PathVariable String uuid) {
        return this.usuarioService.getUsuarioUUID(uuid);
    }

    @PutMapping("/uuid")
    @Operation(
            summary = "Atualizar usuário por UUID",
            description = "Atualiza os dados de um usuário utilizando seu UUID como identificador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public void atualizarUUID(@RequestBody Usuario usuario) {
        this.usuarioService.atualizarUUID(usuario);
    }

}
