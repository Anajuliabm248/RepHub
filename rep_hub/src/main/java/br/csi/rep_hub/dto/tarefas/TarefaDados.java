package br.csi.rep_hub.dto.tarefas;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record TarefaDados(
        @NotBlank(message = "O título da tarefa é obrigatório")
        String titulo,
        String descricao,
        Instant prazo) {
}
