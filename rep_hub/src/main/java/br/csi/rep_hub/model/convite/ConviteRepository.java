package br.csi.rep_hub.model.convite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConviteRepository extends JpaRepository<Convite, Long> {
}
