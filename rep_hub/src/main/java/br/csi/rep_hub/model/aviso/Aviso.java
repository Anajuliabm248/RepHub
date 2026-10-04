package br.csi.rep_hub.model.aviso;

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
@Table(name="aviso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa os avisos de uma república")
public class Aviso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description ="Id de aviso de uma república")
    private Long id;

    @UuidGenerator
    @Schema(description = "UUID de aviso de uma república")
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "republica_id", nullable = false)
    @Schema(description = "República à qual o aviso pertence")
    private Republica republica;

    @NotBlank
    @Schema(description = "Título de um aviso", example = "Mercado")
    private String titulo;

    @NotBlank
    @Schema(description = "Mensagem do aviso", example = "Comprar pão")
    private String mensagem;

    @NotNull
    @CreationTimestamp
    @Schema(description = "data de publicacao do aviso")
    private Instant dataPublicacao;

    @Schema(description = "data de edição do aviso")
    private Instant dataEdicao;

    @Schema(description = "data de remoção do aviso")
    private Instant dataRemocao;
}
