package br.csi.rep_hub.controller;

import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.service.ParticipacaoRepublicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/participacao")
@Tag(name = "Partição Participação de república", description = "Operações relacionadas à partição de participação de repúblicas")
public class ParticipacaoRepublicaController {
    private final ParticipacaoRepublicaService participacaoService;

    public ParticipacaoRepublicaController(ParticipacaoRepublicaService participacaoService) {
        this.participacaoService = participacaoService;
    }

    @GetMapping("/listar")
    @Operation(summary = "Listar participação de repúblicas", description = "Retorna uma lista de todas as participação de repúblicas cadastradas")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de participação de repúblicas retornada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation =  ParticipacaoRepublica.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Nenhuma participação de república encontrado")
    @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor")
    public List< ParticipacaoRepublica> listar(){
        return this.participacaoService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter participação de república", description = "Retorna os detalhes de uma participação de república específico")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Participação de república retornado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation =  ParticipacaoRepublica.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhum participação de república encontrado"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor")
    })
    public  ParticipacaoRepublica participacao(@PathVariable Long id){
        return this.participacaoService.getParticipacaoRepublica(id);
    }

    @PostMapping
    @Operation(
            summary = "Criar um novo participação de república",
            description = "Salva um novo participação de república no banco de dados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Participação de república criado com sucesso"
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
    public void salvar(@RequestBody ParticipacaoRepublica participacao) {
        this.participacaoService.salvar(participacao);
    }

    @PutMapping
    @Operation(
            summary = "Atualizar uma nova participação de república existente",
            description = "Atualiza os dados de um nova participação de república cadastrado no banco de dados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Participação de república atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Participação de república não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public void atualizar(@RequestBody  ParticipacaoRepublica participacao) {
        this.participacaoService.atualizar(participacao);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir participação de república",
            description = "Remove uma participação de república do banco de dados utilizando seu ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Participação de república excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Participação de república não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public void deletar(@PathVariable Long id) {
        this.participacaoService.excluir(id);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(
            summary = "Buscar participação de república por UUID",
            description = "Busca uma participação de república cadastrada utilizando seu UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Participação de república encontrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation =  ParticipacaoRepublica.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Participação de república não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public  ParticipacaoRepublica participacaoUUID(@PathVariable String uuid) {
        return this.participacaoService.getParticipacaoRepublicaUUID(uuid);
    }

    @PutMapping("/uuid")
    @Operation(
            summary = "Atualizar participação de república por UUID",
            description = "Atualiza os dados de uma participação de república utilizando seu UUID como identificador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Participação de república atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Participação de república não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    public void atualizarUUID(@RequestBody  ParticipacaoRepublica participacao) {
        this.participacaoService.atualizarUUID(participacao);
    }
}
