package br.csi.rep_hub.dto;

import jakarta.validation.constraints.NotNull;

public record AtribuicaoTarefaDados(
        @NotNull(message = "A participação responsável é obrigatória")
        Long participacaoResponsavelId) {
}
