package br.csi.rep_hub.service;

import br.csi.rep_hub.dto.extratos.ExtratoColetivoResposta;
import br.csi.rep_hub.dto.extratos.ExtratoIndividualResposta;
import br.csi.rep_hub.dto.extratos.FiltroExtratoDados;
import br.csi.rep_hub.infra.AcessoRepublica;
import br.csi.rep_hub.infra.UsuarioAtual;
import br.csi.rep_hub.model.despesa.Despesa;
import br.csi.rep_hub.model.despesa.DespesaRepository;
import br.csi.rep_hub.model.despesa.ParcelaDespesa;
import br.csi.rep_hub.model.despesa.ParcelaDespesaRepository;
import br.csi.rep_hub.model.extrato.CalculadoraExtrato;
import br.csi.rep_hub.model.extrato.ExtratoFinanceiro;
import br.csi.rep_hub.model.extrato.PeriodoExtrato;
import br.csi.rep_hub.model.pagamento.Pagamento;
import br.csi.rep_hub.model.pagamento.PagamentoRepository;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublicaRepository;
import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.republica.RepublicaRepository;
import br.csi.rep_hub.model.usuario.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

@Service
public class ExtratoFinanceiroService {
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final DespesaRepository despesas;
    private final ParcelaDespesaRepository parcelas;
    private final PagamentoRepository pagamentos;
    private final ParticipacaoRepublicaRepository participacoes;
    private final RepublicaRepository republicas;
    private final UsuarioAtual usuarioAtual;
    private final AcessoRepublica acesso;

    public ExtratoFinanceiroService(DespesaRepository despesas,
                                     ParcelaDespesaRepository parcelas,
                                     PagamentoRepository pagamentos,
                                     ParticipacaoRepublicaRepository participacoes,
                                     RepublicaRepository republicas,
                                     UsuarioAtual usuarioAtual,
                                     AcessoRepublica acesso) {
        this.despesas = despesas;
        this.parcelas = parcelas;
        this.pagamentos = pagamentos;
        this.participacoes = participacoes;
        this.republicas = republicas;
        this.usuarioAtual = usuarioAtual;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public ExtratoIndividualResposta individual(Long republicaId,
                                                 List<UUID> participacaoUuids,
                                                 FiltroExtratoDados filtro) {
        Republica republica = buscarRepublica(republicaId);
        exigirRepublicaAtiva(republica);
        Usuario titular = usuarioAtual.obter();
        List<ParticipacaoRepublica> selecionadas = selecionarParticipacoes(
                republica, titular, participacaoUuids);

        YearMonth atual = mesAtual();
        PeriodoExtrato periodo = resolverPeriodo(
                filtro, mesesDasParticipacoes(selecionadas), atual);
        List<YearMonth> mesesAnteriores = mesesAnteriores(periodo.meses());
        List<Long> participacaoIds = selecionadas.stream()
                .map(ParticipacaoRepublica::getId).toList();

        List<ParcelaDespesa> parcelasConsultadas = parcelas.buscarIncluidasNoPeriodo(
                participacaoIds, inicio(mesesAnteriores.getFirst()),
                fimExclusivo(periodo.fim()));
        List<Pagamento> pagamentosDoPeriodo = pagamentos
                .buscarConfirmadosDasParticipacoesNoPeriodo(
                        participacaoIds, inicio(periodo.inicio()),
                        fimExclusivo(periodo.fim()));
        List<ParcelaDespesa> parcelasDoPeriodo = parcelasConsultadas.stream()
                .filter(parcela -> periodo.meses().contains(YearMonth.from(
                        parcela.getDespesa().getDataReferencia())))
                .toList();

        ExtratoFinanceiro extrato = CalculadoraExtrato.calcularIndividual(
                periodo, mesesAnteriores, parcelasConsultadas, pagamentosDoPeriodo,
                pagamentosPorParcela(parcelasDoPeriodo), atual);
        return ExtratoIndividualResposta.de(
                republica.getUuid(),
                selecionadas.stream().map(ParticipacaoRepublica::getUuid).toList(),
                extrato, atual);
    }

    @Transactional(readOnly = true)
    public ExtratoColetivoResposta coletivo(Long republicaId, FiltroExtratoDados filtro) {
        Republica republica = buscarRepublica(republicaId);
        acesso.exigirParticipante(republica);

        YearMonth atual = mesAtual();
        PeriodoExtrato periodo = resolverPeriodo(
                filtro, mesesEntre(mes(republica.getDataCriacao()), atual), atual);
        List<YearMonth> mesesAnteriores = mesesAnteriores(periodo.meses());
        List<Despesa> despesasConsultadas = despesas.buscarAtivasNoPeriodo(
                republicaId, inicio(mesesAnteriores.getFirst()),
                fimExclusivo(periodo.fim()));
        List<Pagamento> pagamentosDoPeriodo = pagamentos
                .buscarConfirmadosDaRepublicaNoPeriodo(
                        republicaId, inicio(periodo.inicio()),
                        fimExclusivo(periodo.fim()));

        List<Long> despesasDoPeriodo = despesasConsultadas.stream()
                .filter(despesa -> periodo.meses().contains(
                        YearMonth.from(despesa.getDataReferencia())))
                .map(Despesa::getId)
                .toList();
        List<ParcelaDespesa> parcelasDoPeriodo = despesasDoPeriodo.isEmpty()
                ? List.of() : parcelas.buscarIncluidasDasDespesas(despesasDoPeriodo);

        ExtratoFinanceiro extrato = CalculadoraExtrato.calcularColetivo(
                periodo, mesesAnteriores, despesasConsultadas, parcelasDoPeriodo,
                pagamentosDoPeriodo, pagamentosPorParcela(parcelasDoPeriodo), atual);
        return ExtratoColetivoResposta.de(republica.getUuid(), extrato, atual);
    }

    private Map<Long, BigDecimal> pagamentosPorParcela(
            List<ParcelaDespesa> parcelasDoPeriodo) {
        if (parcelasDoPeriodo.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = parcelasDoPeriodo.stream().map(ParcelaDespesa::getId).toList();
        Map<Long, BigDecimal> valores = new HashMap<>();
        for (PagamentoRepository.TotalConfirmadoPorParcela item
                : pagamentos.somarConfirmadosPorParcela(ids)) {
            BigDecimal total = item.getTotal() == null
                    ? new BigDecimal("0.00")
                    : item.getTotal().setScale(2, RoundingMode.HALF_UP);
            valores.put(item.getParcelaId(), total);
        }
        return valores;
    }

    private PeriodoExtrato resolverPeriodo(FiltroExtratoDados filtro,
                                            List<YearMonth> mesesTodoPeriodo,
                                            YearMonth atual) {
        if (filtro == null || filtro.periodo() == null) {
            throw filtroInvalido("Informe o tipo do período");
        }
        List<YearMonth> meses;
        switch (filtro.periodo()) {
            case MES -> {
                if (filtro.mes() == null) {
                    throw filtroInvalido("Informe o mês no formato AAAA-MM");
                }
                meses = List.of(filtro.mes());
            }
            case INTERVALO -> {
                if (filtro.inicio() == null || filtro.fim() == null) {
                    throw filtroInvalido("Informe os meses inicial e final do intervalo");
                }
                if (filtro.inicio().isAfter(filtro.fim())) {
                    throw filtroInvalido("O mês inicial não pode ser posterior ao final");
                }
                meses = mesesEntre(filtro.inicio(), filtro.fim());
            }
            case ANO -> {
                if (filtro.ano() == null) {
                    throw filtroInvalido("Informe o ano");
                }
                try {
                    YearMonth primeiro = YearMonth.of(filtro.ano(), 1);
                    YearMonth ultimo = filtro.ano() == atual.getYear()
                            ? atual : YearMonth.of(filtro.ano(), 12);
                    meses = mesesEntre(primeiro, ultimo);
                } catch (DateTimeException ex) {
                    throw filtroInvalido("Ano inválido");
                }
            }
            case TODO -> meses = mesesTodoPeriodo;
            default -> throw filtroInvalido("Tipo de período inválido");
        }
        if (meses.isEmpty()) {
            throw filtroInvalido("O período não possui meses consultáveis");
        }
        if (meses.getLast().isAfter(atual)) {
            throw filtroInvalido("Não é permitido consultar mês futuro");
        }
        return new PeriodoExtrato(filtro.periodo(), meses);
    }

    private List<ParticipacaoRepublica> selecionarParticipacoes(
            Republica republica, Usuario titular, List<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            List<ParticipacaoRepublica> encontradas = participacoes
                    .findByRepublicaIdAndUsuarioIdOrderByDataEntradaAscIdAsc(
                            republica.getId(), titular.getId());
            if (encontradas.isEmpty()) {
                throw new AccessDeniedException("O usuário não participou desta república");
            }
            return encontradas;
        }

        List<ParticipacaoRepublica> selecionadas = new ArrayList<>();
        for (UUID uuid : new LinkedHashSet<>(uuids)) {
            ParticipacaoRepublica participacao = Optional.ofNullable(
                    participacoes.findParticipacaoRepublicasByUuid(uuid))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Participação não encontrada"));
            if (!participacao.getRepublica().getId().equals(republica.getId())
                    || !participacao.getUsuario().getId().equals(titular.getId())) {
                throw new AccessDeniedException(
                        "O extrato individual é restrito ao próprio titular");
            }
            selecionadas.add(participacao);
        }
        selecionadas.sort(Comparator.comparing(ParticipacaoRepublica::getDataEntrada)
                .thenComparing(ParticipacaoRepublica::getId));
        return selecionadas;
    }

    private List<YearMonth> mesesDasParticipacoes(
            List<ParticipacaoRepublica> participacoesSelecionadas) {
        YearMonth atual = mesAtual();
        SortedSet<YearMonth> meses = new TreeSet<>();
        for (ParticipacaoRepublica participacao : participacoesSelecionadas) {
            YearMonth entrada = mes(participacao.getDataEntrada());
            YearMonth saida = participacao.getDataSaida() == null
                    ? atual : mes(participacao.getDataSaida());
            if (saida.isAfter(atual)) {
                saida = atual;
            }
            if (!entrada.isAfter(saida)) {
                meses.addAll(mesesEntre(entrada, saida));
            }
        }
        return List.copyOf(meses);
    }

    private List<YearMonth> mesesAnteriores(List<YearMonth> selecionados) {
        int quantidade = selecionados.size();
        YearMonth ultimo = selecionados.getFirst().minusMonths(1);
        return mesesEntre(ultimo.minusMonths(quantidade - 1L), ultimo);
    }

    private List<YearMonth> mesesEntre(YearMonth inicio, YearMonth fim) {
        List<YearMonth> meses = new ArrayList<>();
        for (YearMonth mes = inicio; !mes.isAfter(fim); mes = mes.plusMonths(1)) {
            meses.add(mes);
        }
        return meses;
    }

    private LocalDate inicio(YearMonth mes) {
        return mes.atDay(1);
    }

    private LocalDate fimExclusivo(YearMonth mes) {
        return mes.plusMonths(1).atDay(1);
    }

    private YearMonth mes(Instant instante) {
        if (instante == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Registro sem data necessária para o extrato");
        }
        return YearMonth.from(instante.atZone(FUSO));
    }

    private YearMonth mesAtual() {
        return YearMonth.now(FUSO);
    }

    private Republica buscarRepublica(Long id) {
        return republicas.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "República não encontrada"));
    }

    private void exigirRepublicaAtiva(Republica republica) {
        if (!republica.isAtiva()) {
            throw new AccessDeniedException("República encerrada");
        }
    }

    private ResponseStatusException filtroInvalido(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
