package br.csi.rep_hub.dto;

import jakarta.validation.constraints.NotBlank;

public record RepublicaDados(
        @NotBlank
        String nome,

        String descricao)
{}
