package br.csi.rep_hub.model.tarefa;

import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
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
@Table(name="tarefa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa uma tarefa doméstica em uma república")
public class TarefaDomestica {
    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    @Schema(description = "UUID da tarefa de uma república", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uuid;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID da tarefa de uma república", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "republica_id", nullable = false)
    @Schema(description = "República à qual a tarefa pertence")
    private Republica republica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criador_participacao_id", nullable = false, updatable = false)
    @Schema(description = "Participação que criou a tarefa")
    private ParticipacaoRepublica criador;

    @NotBlank
    @Column(nullable = false)
    @Schema(description = "Título de uma tarefa", example = "Banheiro")
    private String titulo;

    @Schema(description = "Descrição de uma tarefa", example = "Limpar o banheiro")
    private String descricao;

    @NotNull
    @CreationTimestamp
    @Schema(description = "Data de criação de uma tarefa")
    private Instant dataCriacao;

    @Schema(description = "Prazo da realização de uma tarefa")
    private Instant prazo;

    @Schema(description = "Data de cancelamento de uma tarefa")
    private Instant dataCancelamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_cancelamento_id")
    @Schema(description = "Participação que cancelou a tarefa")
    private ParticipacaoRepublica autorCancelamento;
}
