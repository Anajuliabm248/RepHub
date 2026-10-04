package br.csi.rep_hub.model.convite;

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
    @Schema(description = "UUID do convite para participar da república")
    private UUID uuid;

    @NotBlank
    @Column(name = "codigo", unique = true, nullable = false)
    private String codigo;

    @Schema(description = "Data de criação do convite")
    private Instant dataCriacao;

    @Schema(description = "Data de expiração do convite")
    private Instant dataExpiracao;

    @Schema(description = "Data de cancelamento do convite")
    private Instant dataCancelamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @NotNull
    @Schema(description = "Status do convite")
    private StatusConvite status;

}
