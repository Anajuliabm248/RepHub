package br.csi.rep_hub.service;

import br.csi.rep_hub.dto.pagamentos.CancelamentoPagamentoDados;
import br.csi.rep_hub.dto.pagamentos.PagamentoDados;
import br.csi.rep_hub.dto.pagamentos.PagamentoResposta;
import br.csi.rep_hub.infra.UsuarioAtual;
import br.csi.rep_hub.model.despesa.Despesa;
import br.csi.rep_hub.model.despesa.DespesaRepository;
import br.csi.rep_hub.model.despesa.ParcelaDespesa;
import br.csi.rep_hub.model.despesa.ParcelaDespesaRepository;
import br.csi.rep_hub.model.pagamento.Pagamento;
import br.csi.rep_hub.model.pagamento.PagamentoRepository;
import br.csi.rep_hub.model.pagamento.StatusPagamento;
import br.csi.rep_hub.model.participacao.PapelRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublicaRepository;
import br.csi.rep_hub.model.participacao.StatusParticipacao;
import br.csi.rep_hub.model.usuario.Usuario;
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
import java.util.List;
import java.util.UUID;

@Service
public class PagamentoService {
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final PagamentoRepository pagamentos;
    private final ParcelaDespesaRepository parcelas;
    private final DespesaRepository despesas;
    private final ParticipacaoRepublicaRepository participacoes;
    private final UsuarioAtual usuarioAtual;

    public PagamentoService(PagamentoRepository pagamentos, ParcelaDespesaRepository parcelas,
                            DespesaRepository despesas,
                            ParticipacaoRepublicaRepository participacoes,
                            UsuarioAtual usuarioAtual) {
        this.pagamentos = pagamentos;
        this.parcelas = parcelas;
        this.despesas = despesas;
        this.participacoes = participacoes;
        this.usuarioAtual = usuarioAtual;
    }

    @Transactional
    public PagamentoResposta cadastrar(Long parcelaId, PagamentoDados dados) {
        ParcelaDespesa referencia = buscarParcela(parcelaId);
        bloquearDespesa(referencia.getDespesa().getId());
        ParcelaDespesa parcela = parcelas.bloquearPorId(parcelaId)
                .orElseThrow(this::parcelaNaoEncontrada);
        return cadastrar(parcela, dados);
    }

    @Transactional
    public PagamentoResposta cadastrar(UUID parcelaUuid, PagamentoDados dados) {
        ParcelaDespesa referencia = buscarParcela(parcelaUuid);
        bloquearDespesa(referencia.getDespesa().getId());
        ParcelaDespesa parcela = parcelas.bloquearPorUuid(parcelaUuid)
                .orElseThrow(this::parcelaNaoEncontrada);
        return cadastrar(parcela, dados);
    }

    @Transactional(readOnly = true)
    public List<PagamentoResposta> listarDaParcela(Long parcelaId) {
        ParcelaDespesa parcela = buscarParcela(parcelaId);
        exigirConsulta(parcela);
        return pagamentos.findByParcelaIdOrderByIdDesc(parcelaId)
                .stream().map(PagamentoResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public PagamentoResposta porId(Long id) {
        Pagamento pagamento = buscar(id);
        exigirConsulta(pagamento.getParcela());
        return PagamentoResposta.de(pagamento);
    }

    @Transactional(readOnly = true)
    public PagamentoResposta porUuid(UUID uuid) {
        Pagamento pagamento = buscar(uuid);
        exigirConsulta(pagamento.getParcela());
        return PagamentoResposta.de(pagamento);
    }

    @Transactional
    public PagamentoResposta editar(Long id, PagamentoDados dados) {
        Pagamento referencia = buscar(id);
        bloquearDespesa(referencia.getParcela().getDespesa().getId());
        return editar(pagamentos.bloquearPorId(id).orElseThrow(this::naoEncontrado), dados);
    }

    @Transactional
    public PagamentoResposta editar(UUID uuid, PagamentoDados dados) {
        Pagamento referencia = buscar(uuid);
        bloquearDespesa(referencia.getParcela().getDespesa().getId());
        return editar(pagamentos.bloquearPorUuid(uuid).orElseThrow(this::naoEncontrado), dados);
    }

    @Transactional
    public void cancelar(Long id, CancelamentoPagamentoDados dados) {
        Pagamento referencia = buscar(id);
        bloquearDespesa(referencia.getParcela().getDespesa().getId());
        cancelar(pagamentos.bloquearPorId(id).orElseThrow(this::naoEncontrado), dados);
    }

    @Transactional
    public void cancelar(UUID uuid, CancelamentoPagamentoDados dados) {
        Pagamento referencia = buscar(uuid);
        bloquearDespesa(referencia.getParcela().getDespesa().getId());
        cancelar(pagamentos.bloquearPorUuid(uuid).orElseThrow(this::naoEncontrado), dados);
    }

    @Transactional
    public PagamentoResposta confirmar(Long id) {
        Pagamento referencia = buscar(id);
        bloquearDespesa(referencia.getParcela().getDespesa().getId());
        return confirmar(pagamentos.bloquearPorId(id).orElseThrow(this::naoEncontrado));
    }

    @Transactional
    public PagamentoResposta confirmar(UUID uuid) {
        Pagamento referencia = buscar(uuid);
        bloquearDespesa(referencia.getParcela().getDespesa().getId());
        return confirmar(pagamentos.bloquearPorUuid(uuid).orElseThrow(this::naoEncontrado));
    }

    private PagamentoResposta cadastrar(ParcelaDespesa parcela, PagamentoDados dados) {
        exigirDevedor(parcela);
        exigirParcelaRecebePagamento(parcela);
        validarDados(dados, parcela);

        Pagamento pagamento = new Pagamento();
        pagamento.setParcela(parcela);
        pagamento.setDataRegistro(Instant.now());
        pagamento.setStatus(StatusPagamento.PENDENTE);
        aplicarDados(pagamento, dados);
        return PagamentoResposta.de(pagamentos.saveAndFlush(pagamento));
    }

    private PagamentoResposta editar(Pagamento pagamento, PagamentoDados dados) {
        exigirDevedor(pagamento.getParcela());
        exigirPendente(pagamento);
        exigirParcelaRecebePagamento(pagamento.getParcela());
        validarDados(dados, pagamento.getParcela());
        aplicarDados(pagamento, dados);
        return PagamentoResposta.de(pagamento);
    }

    private void cancelar(Pagamento pagamento, CancelamentoPagamentoDados dados) {
        participacoes.bloquearParticipacoesAtivas(
                pagamento.getParcela().getDespesa().getRepublica().getId(),
                StatusParticipacao.ATIVO);
        exigirRepublicaAtiva(pagamento.getParcela());
        exigirPendente(pagamento);
        ParticipacaoRepublica ator = exigirDevedorOuAdministrador(pagamento.getParcela());
        pagamento.setStatus(StatusPagamento.CANCELADO);
        pagamento.setCanceladoPor(ator);
        pagamento.setDataCancelamento(Instant.now());
        pagamento.setMotivoCancelamento(dados.motivo().trim());
    }

    private PagamentoResposta confirmar(Pagamento pagamento) {
        participacoes.bloquearParticipacoesAtivas(
                pagamento.getParcela().getDespesa().getRepublica().getId(),
                StatusParticipacao.ATIVO);
        exigirPendente(pagamento);
        exigirParcelaRecebePagamento(pagamento.getParcela());
        ParticipacaoRepublica administrador = exigirAdministrador(pagamento.getParcela());

        BigDecimal confirmado = valorConfirmado(pagamento.getParcela().getId());
        if (confirmado.add(pagamento.getValor())
                .compareTo(pagamento.getParcela().getValor()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O pagamento supera o saldo atual da parcela");
        }
        pagamento.setStatus(StatusPagamento.CONFIRMADO);
        pagamento.setConfirmadoPor(administrador);
        pagamento.setDataConfirmacao(Instant.now());
        return PagamentoResposta.de(pagamento);
    }

    private void validarDados(PagamentoDados dados, ParcelaDespesa parcela) {
        BigDecimal valor = dinheiro(dados.valor());
        if (dados.dataPagamento().isAfter(LocalDate.now(FUSO))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data do pagamento não pode ser futura");
        }
        BigDecimal saldo = parcela.getValor().subtract(valorConfirmado(parcela.getId()));
        if (valor.compareTo(saldo) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O pagamento supera o saldo atual da parcela");
        }
    }

    private void aplicarDados(Pagamento pagamento, PagamentoDados dados) {
        pagamento.setValor(dinheiro(dados.valor()));
        pagamento.setDataPagamento(dados.dataPagamento());
        pagamento.setForma(dados.forma());
        pagamento.setFinalidade(dados.finalidade());
        pagamento.setDestinatario(dados.destinatario().trim());
        pagamento.setObservacao(dados.observacao() == null || dados.observacao().isBlank()
                ? null : dados.observacao().trim());
    }

    private void exigirConsulta(ParcelaDespesa parcela) {
        exigirRepublicaAtiva(parcela);
        Usuario usuario = usuarioAtual.obter();
        if (parcela.getParticipacao().getUsuario().getId().equals(usuario.getId())) {
            return;
        }
        participacoes.findByRepublicaIdAndUsuarioIdAndStatus(
                parcela.getDespesa().getRepublica().getId(), usuario.getId(),
                StatusParticipacao.ATIVO).orElseThrow(() ->
                new AccessDeniedException("Acesso financeiro não autorizado"));
    }

    private void exigirDevedor(ParcelaDespesa parcela) {
        exigirRepublicaAtiva(parcela);
        if (!parcela.getParticipacao().getUsuario().getId()
                .equals(usuarioAtual.obter().getId())) {
            throw new AccessDeniedException("Somente o devedor pode registrar ou editar o pagamento");
        }
    }

    private ParticipacaoRepublica exigirDevedorOuAdministrador(ParcelaDespesa parcela) {
        Usuario usuario = usuarioAtual.obter();
        if (parcela.getParticipacao().getUsuario().getId().equals(usuario.getId())) {
            return parcela.getParticipacao();
        }
        return exigirAdministrador(parcela);
    }

    private ParticipacaoRepublica exigirAdministrador(ParcelaDespesa parcela) {
        exigirRepublicaAtiva(parcela);
        ParticipacaoRepublica ator = participacoes.findByRepublicaIdAndUsuarioIdAndStatus(
                parcela.getDespesa().getRepublica().getId(), usuarioAtual.obter().getId(),
                StatusParticipacao.ATIVO).orElseThrow(() ->
                new AccessDeniedException("Administração ativa necessária"));
        if (ator.getPapel() != PapelRepublica.ADMINISTRADOR) {
            throw new AccessDeniedException("Administração ativa necessária");
        }
        return ator;
    }

    private void exigirParcelaRecebePagamento(ParcelaDespesa parcela) {
        exigirRepublicaAtiva(parcela);
        if (parcela.getDespesa().getDataCancelamento() != null || !parcela.isIncluida()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A parcela não aceita pagamentos");
        }
        if (valorConfirmado(parcela.getId()).compareTo(parcela.getValor()) >= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A parcela já está paga");
        }
    }

    private void exigirRepublicaAtiva(ParcelaDespesa parcela) {
        if (!parcela.getDespesa().getRepublica().isAtiva()) {
            throw new AccessDeniedException("República encerrada");
        }
    }

    private void exigirPendente(Pagamento pagamento) {
        if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Somente pagamentos pendentes podem ser alterados");
        }
    }

    private BigDecimal valorConfirmado(Long parcelaId) {
        BigDecimal valor = pagamentos.somarConfirmados(parcelaId);
        return valor == null ? ZERO : valor.setScale(2);
    }

    private BigDecimal dinheiro(BigDecimal valor) {
        try {
            BigDecimal normalizado = valor.setScale(2, RoundingMode.UNNECESSARY);
            if (normalizado.signum() <= 0 || normalizado.precision() > 12) {
                throw new ArithmeticException();
            }
            return normalizado;
        } catch (ArithmeticException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Valor de pagamento inválido");
        }
    }

    private Despesa bloquearDespesa(Long id) {
        return despesas.bloquearPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não encontrada"));
    }

    private ParcelaDespesa buscarParcela(Long id) {
        return parcelas.findById(id).orElseThrow(this::parcelaNaoEncontrada);
    }

    private ParcelaDespesa buscarParcela(UUID uuid) {
        return parcelas.findByUuid(uuid).orElseThrow(this::parcelaNaoEncontrada);
    }

    private Pagamento buscar(Long id) {
        return pagamentos.findById(id).orElseThrow(this::naoEncontrado);
    }

    private Pagamento buscar(UUID uuid) {
        return pagamentos.findByUuid(uuid).orElseThrow(this::naoEncontrado);
    }

    private ResponseStatusException naoEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado");
    }

    private ResponseStatusException parcelaNaoEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Parcela não encontrada");
    }
}
