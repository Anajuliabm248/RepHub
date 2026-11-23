package br.csi.rep_hub.model.tarefa;

import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "atribuicao_tarefa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa a responsabilidade individual por uma tarefa")
public class AtribuicaoTarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private TarefaDomestica tarefa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participacao_responsavel_id", nullable = false)
    private ParticipacaoRepublica responsavel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_atribuicao_id", nullable = false)
    private ParticipacaoRepublica autorAtribuicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAtribuicao status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant dataAtribuicao;

    private Instant dataConclusao;
    private Instant dataCancelamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_cancelamento_id")
    private ParticipacaoRepublica autorCancelamento;

    // as mudanças de estado ficam na própria entidade para manter suas datas coerentes
    public void concluir(Instant dataConclusao) {
        this.status = StatusAtribuicao.CONCLUIDA;
        this.dataConclusao = dataConclusao;
    }

    public void reabrir() {
        this.status = StatusAtribuicao.PENDENTE;
        this.dataConclusao = null;
    }

    public void cancelar(ParticipacaoRepublica autor, Instant dataCancelamento) {
        this.status = StatusAtribuicao.CANCELADA;
        this.dataConclusao = null;
        this.dataCancelamento = dataCancelamento;
        this.autorCancelamento = autor;
    }
}
