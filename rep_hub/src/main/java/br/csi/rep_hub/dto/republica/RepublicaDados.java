package br.csi.rep_hub.dto.republica;

import jakarta.validation.constraints.NotBlank;

public record RepublicaDados(
        @NotBlank
        String nome,

        String descricao)
{}
