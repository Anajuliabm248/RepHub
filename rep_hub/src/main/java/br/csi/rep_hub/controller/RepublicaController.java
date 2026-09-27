package br.csi.rep_hub.controller;

import br.csi.rep_hub.model.republica. Republica;
import br.csi.rep_hub.service. RepublicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping
@Tag(name = "Republica", description = "Endpoints relacionados a repúblicas")
public class RepublicaController {
    private final  RepublicaService republicaService;

    public RepublicaController(RepublicaService republicaService) {
        this.republicaService = republicaService;
    }


    @GetMapping("/listar")
    @Operation(summary = "Listar repúblicas", description = "Retorna uma lista de todas as repúblicas cadastradas")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de repúblicas retornada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation =  Republica.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Nenhuma república encontrado")
    @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor")
    public List< Republica> listar(){
        return this.republicaService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter república", description = "Retorna os detalhes de um república específico")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "República retornado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation =  Republica.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhum república encontrado"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor")
    })
    public  Republica republica(@PathVariable Long id){
        return this.republicaService.getRepublica(id);
    }

    @PostMapping
    @Transactional
    @Operation(
            summary = "Criar um novo República",
            description = "Salva um novo república no banco de dados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "República criado com sucesso"
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
    public ResponseEntity salvar(@RequestBody  Republica republica, UriComponentsBuilder uriBuilder) {

        this.republicaService.salvar(republica);
        URI uri = uriBuilder.path("/republica/{id}").buildAndExpand(republica.getId()).toUri();
        return ResponseEntity.created(uri).body(republica);
    }

    @PutMapping
    @Transactional
    @Operation(
            summary = "Atualizar um república existente",
            description = "Atualiza os dados de um república cadastrado no banco de dados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "República atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "República não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public ResponseEntity atualizar(@RequestBody  Republica republica) {

        this.republicaService.atualizar(republica);
        return ResponseEntity.ok(republica);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir república",
            description = "Remove um república do banco de dados utilizando seu ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "República excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "República não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public ResponseEntity deletar(@PathVariable Long id) {

        this.republicaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(
            summary = "Buscar república por UUID",
            description = "Busca um república cadastrado utilizando seu UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "República encontrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation =  Republica.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "República não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public  Republica republicaUUID(@PathVariable String uuid) {
        return this.republicaService.getRepublicaUUID(uuid);
    }

    @PutMapping("/uuid")
    @Transactional
    @Operation(
            summary = "Atualizar república por UUID",
            description = "Atualiza os dados de um república utilizando seu UUID como identificador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "República atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "República não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public ResponseEntity atualizarUUID(@RequestBody  Republica republica) {

        this.republicaService.atualizarUUID(republica);
        return ResponseEntity.ok(republica);
    }
}
