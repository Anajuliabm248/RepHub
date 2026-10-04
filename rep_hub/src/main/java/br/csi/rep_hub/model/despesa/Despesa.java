package br.csi.rep_hub.model.despesa;

import br.csi.rep_hub.model.republica.Republica;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="despesa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa as despesas de uma república")
public class Despesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Id da despesa de uma república")
    private Long id;

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    @Schema(description = "UUID da despesa de uma república")
    private  UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "republica_id", nullable = false)
    @Schema(description = "República à qual a despesa pertence")
    private Republica republica;

    @NotBlank
    @Column(nullable = false)
    @Schema(description = "Título de uma despesa", example = "Internet")
    private String titulo;

    @Schema(description = "Descrição de uma despesa")
    private String descricao;

    @NotNull
    @DecimalMin("1.00")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    @Schema(description = "Valor total de uma despesa")
    private BigDecimal valorTotal;

    @NotNull
    @Schema(description = "Data de referência de uma despesa")
    private LocalDate dataReferencia;

    @NotNull
    @Schema(description = "Data de vencimento de uma despesa")
    private LocalDate dataVencimento;

    @NotNull
    @CreationTimestamp
    @Schema(description = "Data de criação de uma despesa")
    private Instant dataCriacao;

    @Schema(description = "Data de cancelamento de uma despesa")
    private Instant dataCancelamento;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "categoria")
    @Schema(description = "Categoria de uma despesa")
    private CategoriaDespesa categoria;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "tipo_divisao", nullable = false)
    @Schema(description = "Tipo de divisão de uma despesa")
    private TipoDivisao tipoDivisao;
}
