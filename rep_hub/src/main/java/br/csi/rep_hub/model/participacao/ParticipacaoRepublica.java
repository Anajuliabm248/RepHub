package br.csi.rep_hub.model.participacao;

import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.usuario.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="participacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa uma participação de um usuário em uma república")
public class ParticipacaoRepublica {
    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    @Schema(description = "UUID da participação da república", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uuid;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID da participação da república", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "republica_id", nullable = false)
    private Republica republica;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel")
    @NotNull
    @Schema(description = "Papel da participação na república", example = "MORADOR")
    private PapelRepublica papel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @NotNull
    @Schema(description = "Status da participação na república", example = "ATIVO")
    private StatusParticipacao status;

    @Schema(description = "Data de entrada da participação na república", example = "2023-01-01T00:00:00")
    @CreationTimestamp
    private Instant dataEntrada;

    private Instant dataSaida;
}
