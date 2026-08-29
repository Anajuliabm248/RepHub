package br.csi.rep_hub.model.participacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="participacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Schema(description = "Representa uma participação de um usuário em uma república")
public class ParticipacaoRepublica {
    @UuidGenerator
    @Schema(description = "UUID da participação da república", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uuid;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID da participação da república", example = "1")
    private Long id;


    @Enumerated
    @Column(name = "papel")
    @NonNull
    @Schema(description = "Papel da participação na república", example = "MORADOR")
    private PapelRepublica papel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @NonNull
    @Schema(description = "Status da participação na república", example = "ATIVO")
    private StatusParticipacao status;

    @NonNull
    @Schema(description = "Data de entrada da participação na república", example = "2023-01-01T00:00:00")
    private LocalDateTime dataEntrada;
}
