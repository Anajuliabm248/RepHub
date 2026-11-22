package br.csi.rep_hub.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsumirConviteDados(
        @NotBlank(message = "O código do convite é obrigatório")
        String codigo) {
}
