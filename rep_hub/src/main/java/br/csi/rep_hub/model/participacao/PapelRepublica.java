package br.csi.rep_hub.model.participacao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representa o papel de um usuário em uma república")
public enum PapelRepublica {
    MORADOR,
    ADMINISTRADOR
}
