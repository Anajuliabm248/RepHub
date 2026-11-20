package br.csi.rep_hub.model.convite;

import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.republica.Republica;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="convite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa um convite para um usuário entrar em uma república")
public class Convite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description ="Id do convite para participar da república")
    private Long id;

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    @Schema(description = "UUID do convite para participar da república")
    private UUID uuid;

    @NotBlank
    @Column(name = "codigo", unique = true, nullable = false)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "republica_id")
    private Republica republica;

    @Schema(description = "Data de criação do convite")
    @CreationTimestamp
    private Instant dataCriacao;

    @Schema(description = "Data de expiração do convite")
    private Instant dataExpiracao;

    @Schema(description = "Data de cancelamento do convite")
    private Instant dataCancelamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelado_por_participacao_id")
    private ParticipacaoRepublica canceladoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @NotNull
    @Schema(description = "Status do convite")
    private StatusConvite status;

}
