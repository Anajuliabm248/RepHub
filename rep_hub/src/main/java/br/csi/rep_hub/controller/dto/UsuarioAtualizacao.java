package br.csi.rep_hub.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioAtualizacao(
        @NotBlank
        String nome,

        @NotBlank @Email
        String email,

        @Size(min = 8) String
        senha
) {}
