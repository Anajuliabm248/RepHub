package br.csi.rep_hub.service;

import br.csi.rep_hub.dto.despesas.ParcelaDespesaResposta;
import br.csi.rep_hub.infra.UsuarioAtual;
import br.csi.rep_hub.model.despesa.ParcelaDespesa;
import br.csi.rep_hub.model.despesa.ParcelaDespesaRepository;
import br.csi.rep_hub.model.pagamento.PagamentoRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ParcelaDespesaService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final ParcelaDespesaRepository parcelas;
    private final PagamentoRepository pagamentos;
    private final ParticipacaoRepublicaRepository participacoes;
    private final UsuarioAtual usuarioAtual;

    public ParcelaDespesaService(ParcelaDespesaRepository parcelas,
                                 PagamentoRepository pagamentos,
                                 ParticipacaoRepublicaRepository participacoes,
                                 UsuarioAtual usuarioAtual) {
        this.parcelas = parcelas;
        this.pagamentos = pagamentos;
        this.participacoes = participacoes;
        this.usuarioAtual = usuarioAtual;
    }

    @Transactional(readOnly = true)
    public ParcelaDespesaResposta porId(Long id) {
        ParcelaDespesa parcela = parcelas.findById(id).orElseThrow(this::naoEncontrada);
        exigirConsulta(parcela.getParticipacao());
        return responder(parcela);
    }

    @Transactional(readOnly = true)
    public ParcelaDespesaResposta porUuid(UUID uuid) {
        ParcelaDespesa parcela = parcelas.findByUuid(uuid).orElseThrow(this::naoEncontrada);
        exigirConsulta(parcela.getParticipacao());
        return responder(parcela);
    }

    @Transactional(readOnly = true)
    public List<ParcelaDespesaResposta> listarDaParticipacao(Long participacaoId) {
        ParticipacaoRepublica participacao = participacoes.findById(participacaoId)
                .orElseThrow(this::participacaoNaoEncontrada);
        exigirConsulta(participacao);
        return parcelas.findByParticipacaoIdOrderByIdDesc(participacaoId)
                .stream().map(this::responder).toList();
    }

    @Transactional(readOnly = true)
    public List<ParcelaDespesaResposta> listarDaParticipacao(UUID participacaoUuid) {
        ParticipacaoRepublica participacao = Optional.ofNullable(
                participacoes.findParticipacaoRepublicasByUuid(participacaoUuid))
                .orElseThrow(this::participacaoNaoEncontrada);
        exigirConsulta(participacao);
        return parcelas.findByParticipacaoIdOrderByIdDesc(participacao.getId())
                .stream().map(this::responder).toList();
    }

    private void exigirConsulta(ParticipacaoRepublica devedora) {
        if (!devedora.getRepublica().isAtiva()) {
            throw new AccessDeniedException("República encerrada");
        }
        Usuario usuario = usuarioAtual.obter();
        if (devedora.getUsuario().getId().equals(usuario.getId())) {
            return;
        }
        participacoes.findByRepublicaIdAndUsuarioIdAndStatus(
                devedora.getRepublica().getId(), usuario.getId(), StatusParticipacao.ATIVO)
                .orElseThrow(() -> new AccessDeniedException("Acesso financeiro não autorizado"));
    }

    private ParcelaDespesaResposta responder(ParcelaDespesa parcela) {
        BigDecimal pago = pagamentos.somarConfirmados(parcela.getId());
        return ParcelaDespesaResposta.de(parcela, pago == null ? ZERO : pago);
    }

    private ResponseStatusException naoEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Parcela não encontrada");
    }

    private ResponseStatusException participacaoNaoEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Participação não encontrada");
    }
}
