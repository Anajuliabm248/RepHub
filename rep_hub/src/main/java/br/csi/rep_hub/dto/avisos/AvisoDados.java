package br.csi.rep_hub.dto.avisos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AvisoDados(
        @NotBlank(message = "O título do aviso é obrigatório")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
        String titulo,

        @NotBlank(message = "A mensagem do aviso é obrigatória")
        @Size(max = 255, message = "A mensagem deve ter no máximo 255 caracteres")
        String mensagem) {
}
