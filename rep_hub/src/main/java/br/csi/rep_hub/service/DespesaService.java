package br.csi.rep_hub.service;

import br.csi.rep_hub.dto.despesas.DespesaDados;
import br.csi.rep_hub.dto.despesas.DespesaResposta;
import br.csi.rep_hub.dto.despesas.ParcelaDespesaDados;
import br.csi.rep_hub.dto.despesas.ParcelaDespesaResposta;
import br.csi.rep_hub.infra.AcessoRepublica;
import br.csi.rep_hub.model.despesa.*;
import br.csi.rep_hub.model.pagamento.Pagamento;
import br.csi.rep_hub.model.pagamento.PagamentoRepository;
import br.csi.rep_hub.model.pagamento.StatusPagamento;
import br.csi.rep_hub.model.participacao.*;
import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.republica.RepublicaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class DespesaService {
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final DespesaRepository despesas;
    private final ParcelaDespesaRepository parcelas;
    private final PagamentoRepository pagamentos;
    private final RepublicaRepository republicas;
    private final ParticipacaoRepublicaRepository participacoes;
    private final AcessoRepublica acesso;

    public DespesaService(DespesaRepository despesas, ParcelaDespesaRepository parcelas,
                          PagamentoRepository pagamentos, RepublicaRepository republicas,
                          ParticipacaoRepublicaRepository participacoes, AcessoRepublica acesso) {
        this.despesas = despesas;
        this.parcelas = parcelas;
        this.pagamentos = pagamentos;
        this.republicas = republicas;
        this.participacoes = participacoes;
        this.acesso = acesso;
    }

    @Transactional
    public DespesaResposta cadastrar(Long republicaId, DespesaDados dados) {
        Republica republica = buscarRepublica(republicaId);
        participacoes.bloquearParticipacoesAtivas(
                republicaId, StatusParticipacao.ATIVO);
        ParticipacaoRepublica criador = acesso.exigirParticipante(republica);
        validarDatasEValor(dados);

        Map<ParticipacaoRepublica, BigDecimal> rateio = montarRateio(null, republica, dados);
        Despesa despesa = new Despesa();
        despesa.setRepublica(republica);
        despesa.setCriador(criador);
        despesa.setDataCriacao(Instant.now());
        aplicarDados(despesa, dados);
        despesas.saveAndFlush(despesa);

        for (Map.Entry<ParticipacaoRepublica, BigDecimal> item : rateio.entrySet()) {
            ParcelaDespesa parcela = new ParcelaDespesa();
            parcela.setDespesa(despesa);
            parcela.setParticipacao(item.getKey());
            parcela.setValor(item.getValue());
            parcela.setIncluida(true);
            parcelas.save(parcela);
        }
        parcelas.flush();
        return responder(despesa);
    }

    @Transactional(readOnly = true)
    public List<DespesaResposta> listar(Long republicaId) {
        Republica republica = buscarRepublica(republicaId);
        acesso.exigirParticipante(republica);
        return despesas.findByRepublicaIdOrderByDataReferenciaDescIdDesc(republicaId)
                .stream().map(this::responder).toList();
    }

    @Transactional(readOnly = true)
    public DespesaResposta porId(Long id) {
        Despesa despesa = buscar(id);
        acesso.exigirParticipante(despesa.getRepublica());
        return responder(despesa);
    }

    @Transactional(readOnly = true)
    public DespesaResposta porUuid(UUID uuid) {
        Despesa despesa = buscar(uuid);
        acesso.exigirParticipante(despesa.getRepublica());
        return responder(despesa);
    }

    @Transactional
    public DespesaResposta alterar(Long id, DespesaDados dados) {
        return alterar(bloquear(id), dados);
    }

    @Transactional
    public DespesaResposta alterar(UUID uuid, DespesaDados dados) {
        return alterar(bloquear(uuid), dados);
    }

    @Transactional
    public void cancelar(Long id) {
        cancelar(bloquear(id));
    }

    @Transactional
    public void cancelar(UUID uuid) {
        cancelar(bloquear(uuid));
    }

    private DespesaResposta alterar(Despesa despesa, DespesaDados dados) {
        participacoes.bloquearParticipacoesAtivas(
                despesa.getRepublica().getId(), StatusParticipacao.ATIVO);
        exigirCriadorOuAdministrador(despesa);
        exigirAtiva(despesa);
        validarDatasEValor(dados);

        if (pagamentos.existsByParcela_Despesa_IdAndStatus(
                despesa.getId(), StatusPagamento.CONFIRMADO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Despesa com pagamento confirmado não pode ser alterada");
        }

        Map<ParticipacaoRepublica, BigDecimal> rateio =
                montarRateio(despesa, despesa.getRepublica(), dados);
        boolean alteracaoFinanceira = houveAlteracaoFinanceira(despesa, dados, rateio);
        if (alteracaoFinanceira && pagamentos.existsByParcela_Despesa_IdAndStatus(
                despesa.getId(), StatusPagamento.PENDENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pagamentos pendentes bloqueiam a alteração do rateio");
        }

        aplicarDados(despesa, dados);
        aplicarRateio(despesa, rateio);
        parcelas.flush();
        return responder(despesa);
    }

    private void cancelar(Despesa despesa) {
        participacoes.bloquearParticipacoesAtivas(
                despesa.getRepublica().getId(), StatusParticipacao.ATIVO);
        ParticipacaoRepublica ator = exigirCriadorOuAdministrador(despesa);
        exigirAtiva(despesa);
        if (pagamentos.existsByParcela_Despesa_IdAndStatus(
                despesa.getId(), StatusPagamento.CONFIRMADO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Despesa com pagamento confirmado não pode ser cancelada");
        }

        Instant agora = Instant.now();
        for (Pagamento pagamento : pagamentos.bloquearPendentesDaDespesa(despesa.getId())) {
            pagamento.setStatus(StatusPagamento.CANCELADO);
            pagamento.setCanceladoPor(ator);
            pagamento.setDataCancelamento(agora);
            pagamento.setMotivoCancelamento("Cancelamento automático da despesa");
        }
        despesa.setDataCancelamento(agora);
        despesa.setCanceladaPor(ator);
    }

    private Map<ParticipacaoRepublica, BigDecimal> montarRateio(
            Despesa despesa, Republica republica, DespesaDados dados) {
        Map<Long, ParcelaDespesa> existentes = new HashMap<>();
        if (despesa != null) {
            for (ParcelaDespesa parcela : parcelas.findByDespesaIdOrderByParticipacaoId(despesa.getId())) {
                existentes.put(parcela.getParticipacao().getId(), parcela);
            }
        }

        Map<Long, ParticipacaoRepublica> selecionadas = new HashMap<>();
        Map<Long, BigDecimal> valoresInformados = new HashMap<>();
        for (ParcelaDespesaDados item : dados.parcelas()) {
            ParticipacaoRepublica participacao = Optional.ofNullable(
                    participacoes.findParticipacaoRepublicasByUuid(item.participacaoUuid()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Participação do rateio não encontrada"));
            if (!participacao.getRepublica().getId().equals(republica.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Todas as parcelas devem pertencer à mesma república");
            }
            if (selecionadas.put(participacao.getId(), participacao) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Uma participação não pode aparecer duas vezes no rateio");
            }
            ParcelaDespesa existente = existentes.get(participacao.getId());
            if (participacao.getStatus() != StatusParticipacao.ATIVO
                    && (existente == null || !existente.isIncluida())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Novas parcelas exigem uma participação ativa");
            }
            if (item.valor() != null) {
                valoresInformados.put(participacao.getId(), dinheiro(item.valor(),
                        "Valor de parcela inválido"));
            }
        }

        // Obrigações de quem já saiu são sempre preservadas, mesmo se omitidas pelo cliente.
        for (ParcelaDespesa existente : existentes.values()) {
            if (existente.isIncluida()
                    && existente.getParticipacao().getStatus() != StatusParticipacao.ATIVO) {
                selecionadas.putIfAbsent(existente.getParticipacao().getId(),
                        existente.getParticipacao());
            }
        }
        if (selecionadas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A despesa precisa ter ao menos uma parcela incluída");
        }

        BigDecimal total = dinheiro(dados.valorTotal(), "Valor total inválido");
        List<ParticipacaoRepublica> ordenadas = selecionadas.values().stream()
                .sorted(Comparator.comparing(ParticipacaoRepublica::getId)).toList();
        Map<ParticipacaoRepublica, BigDecimal> rateio = new LinkedHashMap<>();

        if (dados.tipoDivisao() == TipoDivisao.IGUAL) {
            List<BigDecimal> valores;
            try {
                valores = RateioIgual.dividir(total, ordenadas.size());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O rateio igual criaria parcelas zeradas");
            }
            for (int indice = 0; indice < ordenadas.size(); indice++) {
                rateio.put(ordenadas.get(indice), valores.get(indice));
            }
        } else {
            for (ParticipacaoRepublica participacao : ordenadas) {
                ParcelaDespesa existente = existentes.get(participacao.getId());
                BigDecimal valor = valoresInformados.get(participacao.getId());
                if (valor == null && existente != null && existente.isIncluida()
                        && participacao.getStatus() != StatusParticipacao.ATIVO) {
                    valor = existente.getValor();
                }
                if (valor == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Informe o valor de todas as parcelas personalizadas");
                }
                rateio.put(participacao, valor);
            }
        }

        BigDecimal soma = rateio.values().stream().reduce(ZERO, BigDecimal::add);
        if (soma.compareTo(total) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A soma das parcelas deve ser igual ao valor total");
        }
        for (Map.Entry<ParticipacaoRepublica, BigDecimal> item : rateio.entrySet()) {
            ParcelaDespesa existente = existentes.get(item.getKey().getId());
            if (existente != null && existente.isIncluida()
                    && item.getKey().getStatus() != StatusParticipacao.ATIVO
                    && existente.getValor().compareTo(item.getValue()) != 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Parcelas de ex-moradores não podem ser alteradas");
            }
        }
        return rateio;
    }

    private boolean houveAlteracaoFinanceira(Despesa despesa, DespesaDados dados,
                                               Map<ParticipacaoRepublica, BigDecimal> rateio) {
        if (despesa.getValorTotal().compareTo(dados.valorTotal()) != 0
                || despesa.getTipoDivisao() != dados.tipoDivisao()) {
            return true;
        }
        Map<Long, BigDecimal> atuais = new HashMap<>();
        parcelas.findByDespesaIdOrderByParticipacaoId(despesa.getId()).stream()
                .filter(ParcelaDespesa::isIncluida)
                .forEach(p -> atuais.put(p.getParticipacao().getId(), p.getValor()));
        if (atuais.size() != rateio.size()) {
            return true;
        }
        return rateio.entrySet().stream().anyMatch(e ->
                !e.getValue().equals(atuais.get(e.getKey().getId())));
    }

    private void aplicarRateio(Despesa despesa, Map<ParticipacaoRepublica, BigDecimal> rateio) {
        Map<Long, ParcelaDespesa> existentes = new HashMap<>();
        for (ParcelaDespesa parcela : parcelas.findByDespesaIdOrderByParticipacaoId(despesa.getId())) {
            existentes.put(parcela.getParticipacao().getId(), parcela);
            BigDecimal novoValor = rateio.get(parcela.getParticipacao());
            if (novoValor == null) {
                parcela.setIncluida(false);
            } else {
                parcela.setIncluida(true);
                parcela.setValor(novoValor);
            }
        }
        for (Map.Entry<ParticipacaoRepublica, BigDecimal> item : rateio.entrySet()) {
            if (!existentes.containsKey(item.getKey().getId())) {
                ParcelaDespesa parcela = new ParcelaDespesa();
                parcela.setDespesa(despesa);
                parcela.setParticipacao(item.getKey());
                parcela.setValor(item.getValue());
                parcela.setIncluida(true);
                parcelas.save(parcela);
            }
        }
    }

    private void aplicarDados(Despesa despesa, DespesaDados dados) {
        despesa.setTitulo(dados.titulo().trim());
        despesa.setDescricao(limparOpcional(dados.descricao()));
        despesa.setValorTotal(dinheiro(dados.valorTotal(), "Valor total inválido"));
        despesa.setDataReferencia(dados.dataReferencia());
        despesa.setDataVencimento(dados.dataVencimento());
        despesa.setCategoria(dados.categoria());
        despesa.setTipoDivisao(dados.tipoDivisao());
    }

    private void validarDatasEValor(DespesaDados dados) {
        dinheiro(dados.valorTotal(), "Valor total inválido");
        if (dados.valorTotal().compareTo(BigDecimal.ONE) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O valor total mínimo é R$ 1,00");
        }
        if (dados.dataReferencia().isAfter(LocalDate.now(FUSO))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data de referência não pode ser futura");
        }
    }

    private ParticipacaoRepublica exigirCriadorOuAdministrador(Despesa despesa) {
        ParticipacaoRepublica ator = acesso.exigirParticipante(despesa.getRepublica());
        if (!ator.getId().equals(despesa.getCriador().getId())
                && ator.getPapel() != PapelRepublica.ADMINISTRADOR) {
            throw new AccessDeniedException(
                    "Ação restrita ao criador ou a um administrador da república");
        }
        return ator;
    }

    private void exigirAtiva(Despesa despesa) {
        if (despesa.getDataCancelamento() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Despesa já cancelada");
        }
    }

    private DespesaResposta responder(Despesa despesa) {
        validarLegado(despesa);
        List<ParcelaDespesaResposta> respostas = parcelas
                .findByDespesaIdOrderByParticipacaoId(despesa.getId()).stream()
                .map(parcela -> ParcelaDespesaResposta.de(
                        parcela, valorPago(parcela.getId())))
                .toList();
        return DespesaResposta.de(despesa, respostas);
    }

    private BigDecimal valorPago(Long parcelaId) {
        BigDecimal valor = pagamentos.somarConfirmados(parcelaId);
        return valor == null ? ZERO : valor.setScale(2);
    }

    private BigDecimal dinheiro(BigDecimal valor, String mensagem) {
        try {
            BigDecimal normalizado = valor.setScale(2, RoundingMode.UNNECESSARY);
            if (normalizado.signum() <= 0 || normalizado.precision() > 12) {
                throw new ArithmeticException();
            }
            return normalizado;
        } catch (ArithmeticException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
    }

    private String limparOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private Republica buscarRepublica(Long id) {
        return republicas.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "República não encontrada"));
    }

    private Despesa buscar(Long id) {
        return validarLegado(despesas.findById(id).orElseThrow(this::naoEncontrada));
    }

    private Despesa buscar(UUID uuid) {
        return validarLegado(despesas.findByUuid(uuid).orElseThrow(this::naoEncontrada));
    }

    private Despesa bloquear(Long id) {
        return validarLegado(despesas.bloquearPorId(id).orElseThrow(this::naoEncontrada));
    }

    private Despesa bloquear(UUID uuid) {
        return validarLegado(despesas.bloquearPorUuid(uuid).orElseThrow(this::naoEncontrada));
    }

    private Despesa validarLegado(Despesa despesa) {
        if (despesa.getRepublica() == null || despesa.getCriador() == null) {
            throw naoEncontrada();
        }
        return despesa;
    }

    private ResponseStatusException naoEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não encontrada");
    }
}
