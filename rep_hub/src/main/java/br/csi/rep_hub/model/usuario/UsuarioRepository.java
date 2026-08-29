package br.csi.rep_hub.model.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findUsuariosByUuid(UUID uuid);
    void deleteUsuariosByUuid(UUID uuid);
}
