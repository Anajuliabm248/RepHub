package br.csi.rep_hub.model.convite;

import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.usuario.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "republica_id", nullable = false, updatable = false)
    private Republica republica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emissor_participacao_id", nullable = false, updatable = false)
    private ParticipacaoRepublica emissor;

    @Schema(description = "Data de criação do convite")
    @Column(nullable = false, updatable = false)
    private Instant dataCriacao;

    @Schema(description = "Data de expiração do convite")
    @Column(nullable = false, updatable = false)
    private Instant dataExpiracao;

    @Schema(description = "Data em que o convite foi utilizado")
    private Instant dataUtilizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizado_por_usuario_id")
    private Usuario utilizadoPor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participacao_criada_id", unique = true)
    private ParticipacaoRepublica participacaoCriada;

    @Schema(description = "Data de cancelamento do convite")
    private Instant dataCancelamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelado_por_participacao_id")
    private ParticipacaoRepublica canceladoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    @Schema(description = "Status do convite")
    private StatusConvite status;

}
