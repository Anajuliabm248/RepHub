package br.csi.rep_hub.dto.pagamentos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelamentoPagamentoDados(
        @NotBlank(message = "O motivo do cancelamento é obrigatório")
        @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres")
        String motivo) {
}
