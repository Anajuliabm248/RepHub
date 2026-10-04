package br.csi.rep_hub.controller.dto;

import br.csi.rep_hub.model.usuario.Usuario;
import java.time.Instant;
import java.util.UUID;

// Devolve o usuário sem mostrar sua senha
public record UsuarioResposta(Long id, UUID uuid, String nome, String email,
                              Instant dataCadastro, boolean ativo) {
    public static UsuarioResposta de(Usuario usuario) {
        return new UsuarioResposta(usuario.getId(), usuario.getUuid(), usuario.getNome(),
                usuario.getEmail(), usuario.getDataCadastro(), usuario.isAtivo());
    }
}
