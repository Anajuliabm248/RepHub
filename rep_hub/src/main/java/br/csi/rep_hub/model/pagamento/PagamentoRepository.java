package br.csi.rep_hub.model.pagamento;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    Optional<Pagamento> findByUuid(UUID uuid);
    List<Pagamento> findByParcelaIdOrderByIdDesc(Long parcelaId);
    boolean existsByParcela_Despesa_IdAndStatus(Long despesaId, StatusPagamento status);

    @Query("select coalesce(sum(p.valor), 0) from Pagamento p where p.parcela.id = :parcelaId " +
            "and p.status = br.csi.rep_hub.model.pagamento.StatusPagamento.CONFIRMADO")
    BigDecimal somarConfirmados(@Param("parcelaId") Long parcelaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pagamento p where p.id = :id")
    Optional<Pagamento> bloquearPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pagamento p where p.uuid = :uuid")
    Optional<Pagamento> bloquearPorUuid(@Param("uuid") UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pagamento p where p.parcela.despesa.id = :despesaId and " +
            "p.status = br.csi.rep_hub.model.pagamento.StatusPagamento.PENDENTE order by p.id")
    List<Pagamento> bloquearPendentesDaDespesa(@Param("despesaId") Long despesaId);

    @Query("select count(p) from Pagamento p where p.parcela.participacao.usuario.id = :usuarioId " +
            "and p.status = br.csi.rep_hub.model.pagamento.StatusPagamento.PENDENTE")
    long contarPendentesDoUsuario(@Param("usuarioId") Long usuarioId);

    @Query("select p from Pagamento p where p.parcela.participacao.id in :participacaoIds " +
            "and p.status = br.csi.rep_hub.model.pagamento.StatusPagamento.CONFIRMADO " +
            "and p.dataPagamento >= :inicio and p.dataPagamento < :fim " +
            "order by p.dataPagamento, p.id")
    List<Pagamento> buscarConfirmadosDasParticipacoesNoPeriodo(
            @Param("participacaoIds") List<Long> participacaoIds,
            @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("select p from Pagamento p where p.parcela.despesa.republica.id = :republicaId " +
            "and p.status = br.csi.rep_hub.model.pagamento.StatusPagamento.CONFIRMADO " +
            "and p.dataPagamento >= :inicio and p.dataPagamento < :fim " +
            "order by p.dataPagamento, p.id")
    List<Pagamento> buscarConfirmadosDaRepublicaNoPeriodo(
            @Param("republicaId") Long republicaId,
            @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("select p.parcela.id as parcelaId, sum(p.valor) as total " +
            "from Pagamento p where p.parcela.id in :parcelaIds " +
            "and p.status = br.csi.rep_hub.model.pagamento.StatusPagamento.CONFIRMADO " +
            "group by p.parcela.id")
    List<TotalConfirmadoPorParcela> somarConfirmadosPorParcela(
            @Param("parcelaIds") List<Long> parcelaIds);

    interface TotalConfirmadoPorParcela {
        Long getParcelaId();
        BigDecimal getTotal();
    }
}
