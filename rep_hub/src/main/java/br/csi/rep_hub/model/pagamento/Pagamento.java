package br.csi.rep_hub.model.pagamento;

import br.csi.rep_hub.model.despesa.ParcelaDespesa;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pagamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcela_id", nullable = false)
    private ParcelaDespesa parcela;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @Column(name = "data_registro", nullable = false, updatable = false)
    private Instant dataRegistro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento forma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinalidadePagamento finalidade;

    @Column(nullable = false)
    private String destinatario;

    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmado_por_id")
    private ParticipacaoRepublica confirmadoPor;

    private Instant dataConfirmacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelado_por_id")
    private ParticipacaoRepublica canceladoPor;

    private Instant dataCancelamento;

    private String motivoCancelamento;
}
