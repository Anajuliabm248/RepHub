package br.csi.rep_hub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// dados recebidos da autenticação
public record LoginDados(
        @NotBlank @Email String email,
        @NotBlank String senha
) {
}
