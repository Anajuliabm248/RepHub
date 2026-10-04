package br.csi.rep_hub.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record RepublicaDados(
        @NotBlank
        String nome,

        String descricao)
{}
