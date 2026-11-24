package br.csi.rep_hub.model.despesa;

import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "parcela_despesa", uniqueConstraints =
        @UniqueConstraint(name = "uk_parcela_despesa_participacao",
                columnNames = {"despesa_id", "participacao_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParcelaDespesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "despesa_id", nullable = false)
    private Despesa despesa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participacao_id", nullable = false)
    private ParticipacaoRepublica participacao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private boolean incluida = true;

    public BigDecimal saldo(BigDecimal valorPago) {
        if (!incluida || despesa.getDataCancelamento() != null) {
            return BigDecimal.ZERO.setScale(2);
        }
        return valor.subtract(valorPago).setScale(2);
    }

    public SituacaoParcela situacao(BigDecimal valorPago) {
        if (!incluida || despesa.getDataCancelamento() != null) {
            return SituacaoParcela.CANCELADA;
        }
        if (valorPago.signum() == 0) {
            return SituacaoParcela.PENDENTE;
        }
        return valorPago.compareTo(valor) >= 0
                ? SituacaoParcela.PAGA : SituacaoParcela.PARCIALMENTE_PAGA;
    }
}
