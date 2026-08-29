package br.csi.rep_hub.model.republica;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RepublicaRepository extends JpaRepository<Republica, Long> {
    Republica findRepublicaByUuid(UUID uuid);
    void deleteRepublicaByUuid(UUID uuid);
}
