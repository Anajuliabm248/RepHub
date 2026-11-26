package br.csi.rep_hub.model.extrato;

import br.csi.rep_hub.model.despesa.CategoriaDespesa;
import br.csi.rep_hub.model.despesa.Despesa;
import br.csi.rep_hub.model.despesa.ParcelaDespesa;
import br.csi.rep_hub.model.pagamento.Pagamento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;

public final class CalculadoraExtrato {
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private CalculadoraExtrato() {
    }

    public static ExtratoFinanceiro calcularIndividual(
            PeriodoExtrato periodo,
            List<YearMonth> mesesAnteriores,
            List<ParcelaDespesa> parcelasConsultadas,
            List<Pagamento> pagamentosDoPeriodo,
            Map<Long, BigDecimal> pagamentosPorParcela,
            YearMonth mesAtual) {
        Acumuladores valores = novosAcumuladores(periodo.meses());
        Set<YearMonth> mesesSelecionados = new HashSet<>(periodo.meses());
        Set<YearMonth> anteriores = new HashSet<>(mesesAnteriores);
        BigDecimal custoAnterior = ZERO;

        for (ParcelaDespesa parcela : parcelasConsultadas) {
            YearMonth mes = YearMonth.from(parcela.getDespesa().getDataReferencia());
            if (mesesSelecionados.contains(mes)) {
                somar(valores.custos(), mes, parcela.getValor());
                somarCategoria(valores.categorias(),
                        parcela.getDespesa().getCategoria(), parcela.getValor());
                BigDecimal valorPago = pagamentosPorParcela.getOrDefault(
                        parcela.getId(), ZERO);
                somar(valores.saldos(), mes, parcela.saldo(dinheiro(valorPago)));
            } else if (anteriores.contains(mes)) {
                custoAnterior = custoAnterior.add(parcela.getValor());
            }
        }

        preencherPagamentos(valores.pagamentos(), pagamentosDoPeriodo, mesesSelecionados);
        return finalizar(periodo, mesesAnteriores, valores,
                dinheiro(custoAnterior), mesAtual);
    }

    public static ExtratoFinanceiro calcularColetivo(
            PeriodoExtrato periodo,
            List<YearMonth> mesesAnteriores,
            List<Despesa> despesasConsultadas,
            List<ParcelaDespesa> parcelasDoPeriodo,
            List<Pagamento> pagamentosDoPeriodo,
            Map<Long, BigDecimal> pagamentosPorParcela,
            YearMonth mesAtual) {
        Acumuladores valores = novosAcumuladores(periodo.meses());
        Set<YearMonth> mesesSelecionados = new HashSet<>(periodo.meses());
        Set<YearMonth> anteriores = new HashSet<>(mesesAnteriores);
        BigDecimal custoAnterior = ZERO;

        // A despesa é a raiz do custo coletivo e, portanto, é contada uma única vez.
        for (Despesa despesa : despesasConsultadas) {
            YearMonth mes = YearMonth.from(despesa.getDataReferencia());
            if (mesesSelecionados.contains(mes)) {
                somar(valores.custos(), mes, despesa.getValorTotal());
                somarCategoria(valores.categorias(),
                        despesa.getCategoria(), despesa.getValorTotal());
            } else if (anteriores.contains(mes)) {
                custoAnterior = custoAnterior.add(despesa.getValorTotal());
            }
        }

        for (ParcelaDespesa parcela : parcelasDoPeriodo) {
            YearMonth mes = YearMonth.from(parcela.getDespesa().getDataReferencia());
            BigDecimal valorPago = pagamentosPorParcela.getOrDefault(
                    parcela.getId(), ZERO);
            somar(valores.saldos(), mes, parcela.saldo(dinheiro(valorPago)));
        }
        preencherPagamentos(valores.pagamentos(), pagamentosDoPeriodo, mesesSelecionados);
        return finalizar(periodo, mesesAnteriores, valores,
                dinheiro(custoAnterior), mesAtual);
    }

    private static ExtratoFinanceiro finalizar(
            PeriodoExtrato periodo,
            List<YearMonth> mesesAnteriores,
            Acumuladores valores,
            BigDecimal custoAnterior,
            YearMonth mesAtual) {
        BigDecimal custoTotal = total(valores.custos().values());
        IndicadoresFinanceiros indicadores = criarIndicadores(valores);
        List<SerieMensal> serie = criarSerie(valores, mesAtual);
        List<GastoCategoria> categorias = criarCategorias(
                valores.categorias(), custoTotal);
        ComparacaoPeriodo comparacao = criarComparacao(
                custoTotal, custoAnterior, mesesAnteriores);
        return new ExtratoFinanceiro(
                periodo, indicadores, serie, categorias, comparacao);
    }

    private static IndicadoresFinanceiros criarIndicadores(Acumuladores valores) {
        List<BigDecimal> custos = new ArrayList<>(valores.custos().values());
        BigDecimal maior = custos.stream().max(BigDecimal::compareTo).orElse(ZERO);
        BigDecimal menor = custos.stream().min(BigDecimal::compareTo).orElse(ZERO);
        List<BigDecimal> mesesComCusto = custos.stream()
                .filter(valor -> valor.signum() > 0).toList();
        BigDecimal media = mesesComCusto.isEmpty() ? ZERO
                : total(mesesComCusto).divide(
                        BigDecimal.valueOf(mesesComCusto.size()), 2, RoundingMode.HALF_UP);
        return new IndicadoresFinanceiros(
                total(custos), total(valores.pagamentos().values()),
                total(valores.saldos().values()), dinheiro(media),
                dinheiro(maior), dinheiro(menor));
    }

    private static List<SerieMensal> criarSerie(
            Acumuladores valores, YearMonth mesAtual) {
        return valores.custos().keySet().stream()
                .map(mes -> new SerieMensal(
                        mes, dinheiro(valores.custos().get(mes)),
                        dinheiro(valores.pagamentos().get(mes)),
                        dinheiro(valores.saldos().get(mes)), mes.equals(mesAtual)))
                .toList();
    }

    private static List<GastoCategoria> criarCategorias(
            EnumMap<CategoriaDespesa, BigDecimal> valores, BigDecimal total) {
        List<GastoCategoria> categorias = new ArrayList<>();
        for (CategoriaDespesa categoria : CategoriaDespesa.values()) {
            BigDecimal valor = dinheiro(valores.get(categoria));
            BigDecimal percentual = total.signum() == 0 ? ZERO
                    : valor.multiply(BigDecimal.valueOf(100))
                    .divide(total, 2, RoundingMode.HALF_UP);
            categorias.add(new GastoCategoria(
                    categoria, valor, percentual.setScale(2)));
        }
        return categorias;
    }

    private static ComparacaoPeriodo criarComparacao(
            BigDecimal custoAtual,
            BigDecimal custoAnterior,
            List<YearMonth> mesesAnteriores) {
        int resultado = custoAtual.compareTo(custoAnterior);
        TendenciaFinanceira tendencia = resultado > 0 ? TendenciaFinanceira.AUMENTO
                : resultado < 0 ? TendenciaFinanceira.DIMINUICAO
                : TendenciaFinanceira.ESTABILIDADE;
        BigDecimal variacao = custoAnterior.signum() == 0 ? null
                : custoAtual.subtract(custoAnterior).multiply(BigDecimal.valueOf(100))
                .divide(custoAnterior, 2, RoundingMode.HALF_UP);
        return new ComparacaoPeriodo(
                mesesAnteriores.getFirst(), mesesAnteriores.getLast(),
                dinheiro(custoAnterior),
                dinheiro(custoAtual.subtract(custoAnterior).abs()),
                variacao, tendencia);
    }

    private static void preencherPagamentos(
            Map<YearMonth, BigDecimal> destino,
            List<Pagamento> confirmados,
            Set<YearMonth> mesesSelecionados) {
        for (Pagamento pagamento : confirmados) {
            YearMonth mes = YearMonth.from(pagamento.getDataPagamento());
            if (mesesSelecionados.contains(mes)) {
                somar(destino, mes, pagamento.getValor());
            }
        }
    }

    private static Acumuladores novosAcumuladores(List<YearMonth> meses) {
        return new Acumuladores(
                mapaZerado(meses), mapaZerado(meses), mapaZerado(meses),
                categoriasZeradas());
    }

    private static Map<YearMonth, BigDecimal> mapaZerado(List<YearMonth> meses) {
        Map<YearMonth, BigDecimal> valores = new LinkedHashMap<>();
        meses.forEach(mes -> valores.put(mes, ZERO));
        return valores;
    }

    private static EnumMap<CategoriaDespesa, BigDecimal> categoriasZeradas() {
        EnumMap<CategoriaDespesa, BigDecimal> valores =
                new EnumMap<>(CategoriaDespesa.class);
        for (CategoriaDespesa categoria : CategoriaDespesa.values()) {
            valores.put(categoria, ZERO);
        }
        return valores;
    }

    private static void somar(
            Map<YearMonth, BigDecimal> valores, YearMonth mes, BigDecimal valor) {
        valores.merge(mes, dinheiro(valor), BigDecimal::add);
    }

    private static void somarCategoria(
            EnumMap<CategoriaDespesa, BigDecimal> valores,
            CategoriaDespesa categoria,
            BigDecimal valor) {
        valores.merge(categoria, dinheiro(valor), BigDecimal::add);
    }

    private static BigDecimal total(Collection<BigDecimal> valores) {
        return dinheiro(valores.stream().reduce(ZERO, BigDecimal::add));
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return (valor == null ? ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    private record Acumuladores(
            Map<YearMonth, BigDecimal> custos,
            Map<YearMonth, BigDecimal> pagamentos,
            Map<YearMonth, BigDecimal> saldos,
            EnumMap<CategoriaDespesa, BigDecimal> categorias) {
    }
}
