package br.csi.rep_hub.service;

import br.csi.rep_hub.dto.extratos.*;
import br.csi.rep_hub.infra.AcessoRepublica;
import br.csi.rep_hub.infra.UsuarioAtual;
import br.csi.rep_hub.model.despesa.*;
import br.csi.rep_hub.model.extrato.TendenciaFinanceira;
import br.csi.rep_hub.model.extrato.TipoPeriodoExtrato;
import br.csi.rep_hub.model.pagamento.Pagamento;
import br.csi.rep_hub.model.pagamento.PagamentoRepository;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublicaRepository;
import br.csi.rep_hub.model.participacao.StatusParticipacao;
import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.republica.RepublicaRepository;
import br.csi.rep_hub.model.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtratoFinanceiroServiceTest {
    @Mock private DespesaRepository despesas;
    @Mock private ParcelaDespesaRepository parcelas;
    @Mock private PagamentoRepository pagamentos;
    @Mock private ParticipacaoRepublicaRepository participacoes;
    @Mock private RepublicaRepository republicas;
    @Mock private UsuarioAtual usuarioAtual;
    @Mock private AcessoRepublica acesso;

    private ExtratoFinanceiroService service;
    private Republica republica;
    private Usuario usuario;
    private ParticipacaoRepublica participacao;

    @BeforeEach
    void preparar() {
        service = new ExtratoFinanceiroService(despesas, parcelas, pagamentos,
                participacoes, republicas, usuarioAtual, acesso);

        republica = new Republica();
        republica.setId(1L);
        republica.setUuid(UUID.randomUUID());
        republica.setAtiva(true);
        republica.setDataCriacao(Instant.parse("2025-01-01T03:00:00Z"));

        usuario = new Usuario();
        usuario.setId(2L);
        usuario.setUuid(UUID.randomUUID());
        usuario.setAtivo(true);

        participacao = new ParticipacaoRepublica();
        participacao.setId(3L);
        participacao.setUuid(UUID.randomUUID());
        participacao.setUsuario(usuario);
        participacao.setRepublica(republica);
        participacao.setStatus(StatusParticipacao.INATIVO);
        participacao.setDataEntrada(Instant.parse("2025-06-01T03:00:00Z"));
        participacao.setDataSaida(Instant.parse("2025-08-20T15:00:00Z"));
    }

    @Test
    void custoDeAgostoUsaPagamentoPosteriorParaCalcularOSaldo() {
        ParcelaDespesa parcela = parcela(10L, despesa(20L, "100.00", 8), "100.00");
        quandoConsultaIndividual(List.of(parcela), List.of());
        when(pagamentos.somarConfirmadosPorParcela(List.of(10L)))
                .thenReturn(List.of(totalPago(10L, "40.00")));

        ExtratoIndividualResposta resposta = service.individual(1L, null,
                filtroMes(YearMonth.of(2025, 8)));

        assertEquals(new BigDecimal("100.00"), resposta.indicadores().custoTotal());
        assertEquals(new BigDecimal("0.00"), resposta.indicadores().pagamentosConfirmados());
        assertEquals(new BigDecimal("60.00"), resposta.indicadores().saldoAtual());
        assertEquals(new BigDecimal("100.00"), resposta.indicadores().mediaMensal());
        assertNull(resposta.comparacao().variacaoPercentual());
        assertEquals(TendenciaFinanceira.AUMENTO, resposta.comparacao().tendencia());
    }

    @Test
    void pagamentoDepoisDaSaidaApareceNoMesExplicitamenteSelecionado() {
        ParcelaDespesa parcelaAgosto = parcela(
                10L, despesa(20L, "100.00", 8), "100.00");
        Pagamento pagamentoSetembro = new Pagamento();
        pagamentoSetembro.setValor(new BigDecimal("40.00"));
        pagamentoSetembro.setDataPagamento(LocalDate.of(2025, 9, 5));
        quandoConsultaIndividual(List.of(parcelaAgosto), List.of(pagamentoSetembro));

        ExtratoIndividualResposta resposta = service.individual(1L, null,
                filtroMes(YearMonth.of(2025, 9)));

        assertEquals(new BigDecimal("0.00"), resposta.indicadores().custoTotal());
        assertEquals(new BigDecimal("40.00"), resposta.indicadores().pagamentosConfirmados());
        assertEquals(new BigDecimal("0.00"), resposta.indicadores().saldoAtual());
        assertEquals(new BigDecimal("100.00"),
                resposta.comparacao().custoPeriodoAnterior());
        verify(pagamentos, never()).somarConfirmadosPorParcela(anyList());
    }

    @Test
    void todoPeriodoTerminaNoMesDaSaida() {
        quandoConsultaIndividual(List.of(), List.of());

        ExtratoIndividualResposta resposta = service.individual(1L, null,
                new FiltroExtratoDados(TipoPeriodoExtrato.TODO,
                        null, null, null, null));

        assertEquals(YearMonth.of(2025, 6), resposta.periodo().inicio());
        assertEquals(YearMonth.of(2025, 8), resposta.periodo().fim());
        assertEquals(3, resposta.periodo().quantidadeMeses());
        verify(pagamentos).buscarConfirmadosDasParticipacoesNoPeriodo(
                List.of(3L), LocalDate.of(2025, 6, 1), LocalDate.of(2025, 9, 1));
    }

    @Test
    void coletivoContaDespesaUmaVezMesmoComDuasParcelas() {
        participacao.setStatus(StatusParticipacao.ATIVO);
        Despesa despesa = despesa(20L, "100.00", 8);
        ParcelaDespesa primeira = parcela(10L, despesa, "50.00");
        ParcelaDespesa segunda = parcela(11L, despesa, "50.00");
        when(republicas.findById(1L)).thenReturn(Optional.of(republica));
        when(despesas.buscarAtivasNoPeriodo(eq(1L), any(), any()))
                .thenReturn(List.of(despesa));
        when(pagamentos.buscarConfirmadosDaRepublicaNoPeriodo(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(parcelas.buscarIncluidasDasDespesas(List.of(20L)))
                .thenReturn(List.of(primeira, segunda));
        when(pagamentos.somarConfirmadosPorParcela(List.of(10L, 11L)))
                .thenReturn(List.of());

        ExtratoColetivoResposta resposta = service.coletivo(1L,
                filtroMes(YearMonth.of(2025, 8)));

        assertEquals(new BigDecimal("100.00"), resposta.indicadores().custoTotal());
        assertEquals(new BigDecimal("100.00"), resposta.indicadores().saldoAtual());
        assertEquals(new BigDecimal("100.00"), resposta.categorias().stream()
                .filter(item -> item.categoria() == CategoriaDespesa.MERCADO)
                .findFirst().orElseThrow().percentual());
        verify(acesso).exigirParticipante(republica);
    }

    @Test
    void republicaEncerradaBloqueiaAteOExtratoDoExMorador() {
        republica.setAtiva(false);
        when(republicas.findById(1L)).thenReturn(Optional.of(republica));

        assertThrows(AccessDeniedException.class, () -> service.individual(
                1L, null, filtroMes(YearMonth.of(2025, 8))));
        verifyNoInteractions(usuarioAtual, participacoes, parcelas, pagamentos);
    }

    @Test
    void administradorNaoPodeConsultarExtratoIndividualAlheio() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(99L);
        ParticipacaoRepublica participacaoAlheia = new ParticipacaoRepublica();
        participacaoAlheia.setId(98L);
        participacaoAlheia.setUuid(UUID.randomUUID());
        participacaoAlheia.setRepublica(republica);
        participacaoAlheia.setUsuario(outroUsuario);
        when(republicas.findById(1L)).thenReturn(Optional.of(republica));
        when(usuarioAtual.obter()).thenReturn(usuario);
        when(participacoes.findParticipacaoRepublicasByUuid(participacaoAlheia.getUuid()))
                .thenReturn(participacaoAlheia);

        assertThrows(AccessDeniedException.class, () -> service.individual(
                1L, List.of(participacaoAlheia.getUuid()),
                filtroMes(YearMonth.of(2025, 8))));
        verifyNoInteractions(parcelas, pagamentos);
    }

    @Test
    void filtroNaoAceitaMesFuturo() {
        when(republicas.findById(1L)).thenReturn(Optional.of(republica));
        when(usuarioAtual.obter()).thenReturn(usuario);
        when(participacoes.findByRepublicaIdAndUsuarioIdOrderByDataEntradaAscIdAsc(1L, 2L))
                .thenReturn(List.of(participacao));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.individual(1L, null,
                        filtroMes(YearMonth.now().plusMonths(1))));

        assertEquals(400, erro.getStatusCode().value());
        verifyNoInteractions(parcelas, pagamentos);
    }

    private void quandoConsultaIndividual(List<ParcelaDespesa> parcelasEncontradas,
                                          List<Pagamento> pagamentosEncontrados) {
        when(republicas.findById(1L)).thenReturn(Optional.of(republica));
        when(usuarioAtual.obter()).thenReturn(usuario);
        when(participacoes.findByRepublicaIdAndUsuarioIdOrderByDataEntradaAscIdAsc(1L, 2L))
                .thenReturn(List.of(participacao));
        when(parcelas.buscarIncluidasNoPeriodo(eq(List.of(3L)), any(), any()))
                .thenReturn(parcelasEncontradas);
        when(pagamentos.buscarConfirmadosDasParticipacoesNoPeriodo(
                eq(List.of(3L)), any(), any())).thenReturn(pagamentosEncontrados);
    }

    private FiltroExtratoDados filtroMes(YearMonth mes) {
        return new FiltroExtratoDados(TipoPeriodoExtrato.MES, mes, null, null, null);
    }

    private Despesa despesa(Long id, String valor, int mes) {
        Despesa despesa = new Despesa();
        despesa.setId(id);
        despesa.setRepublica(republica);
        despesa.setValorTotal(new BigDecimal(valor));
        despesa.setDataReferencia(LocalDate.of(2025, mes, 1));
        despesa.setCategoria(CategoriaDespesa.MERCADO);
        return despesa;
    }

    private ParcelaDespesa parcela(Long id, Despesa despesa, String valor) {
        ParcelaDespesa parcela = new ParcelaDespesa();
        parcela.setId(id);
        parcela.setDespesa(despesa);
        parcela.setParticipacao(participacao);
        parcela.setValor(new BigDecimal(valor));
        parcela.setIncluida(true);
        return parcela;
    }

    private PagamentoRepository.TotalConfirmadoPorParcela totalPago(
            Long parcelaId, String total) {
        return new PagamentoRepository.TotalConfirmadoPorParcela() {
            @Override
            public Long getParcelaId() {
                return parcelaId;
            }

            @Override
            public BigDecimal getTotal() {
                return new BigDecimal(total);
            }
        };
    }
}
