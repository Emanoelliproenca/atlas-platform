package com.atlas.platform.service;

import com.atlas.platform.dto.ContratoServicoRequest;
import com.atlas.platform.dto.ContratoServicoResponse;
import com.atlas.platform.exception.BusinessException;
import com.atlas.platform.exception.ResourceNotFoundException;
import com.atlas.platform.mapper.ContratoServicoMapper;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.ContratoServico;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ContratoRepository;
import com.atlas.platform.repository.ContratoServicoRepository;
import com.atlas.platform.repository.ServicoRepository;
import com.atlas.platform.util.TextoUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ContratoServicoService {

    private static final Comparator<ContratoServico> ORDENACAO_POR_CONTRATO_E_SERVICO = Comparator
            .comparing((ContratoServico item) -> item.getContrato().getNome(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(item -> item.getServico().getNome(), String.CASE_INSENSITIVE_ORDER);

    private final ContratoServicoRepository repository;
    private final ContratoRepository contratoRepository;
    private final ServicoRepository servicoRepository;
    private final ContratoServicoMapper contratoServicoMapper;
    private final AuditoriaService auditoriaService;

    public ContratoServicoService(
            ContratoServicoRepository repository,
            ContratoRepository contratoRepository,
            ServicoRepository servicoRepository,
            ContratoServicoMapper contratoServicoMapper,
            AuditoriaService auditoriaService
    ) {
        this.repository = repository;
        this.contratoRepository = contratoRepository;
        this.servicoRepository = servicoRepository;
        this.contratoServicoMapper = contratoServicoMapper;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ContratoServicoResponse salvar(ContratoServicoRequest request) {
        if (repository.existsByContratoIdAndServicoId(request.getContratoId(), request.getServicoId())) {
            throw new BusinessException("Esse serviço já está vinculado a esse contrato");
        }

        Contrato contrato = buscarContrato(request.getContratoId());
        Servico servico = buscarServico(request.getServicoId());

        ContratoServico contratoServico = new ContratoServico();
        aplicarVinculo(contratoServico, contrato, servico, request);

        ContratoServico salvo = repository.save(contratoServico);
        auditoriaService.registrar("CONTRATO_SERVICO", salvo.getId(), "CREATE_RELACIONAMENTO",
                "Relacionamento criado entre contrato " + contrato.getNome() + " e serviço " + servico.getNome());
        return contratoServicoMapper.toResponse(salvo);
    }

    public List<ContratoServicoResponse> listar(
            List<Long> contratoIds,
            List<Long> servicoIds,
            String grupo,
            String setor,
            String contratoNome,
            String servicoNome
    ) {
        FiltrosRelacionamento filtros = new FiltrosRelacionamento(grupo, setor, contratoNome, servicoNome);

        return buscarRelacionamentos(contratoIds, servicoIds).stream()
                .filter(item -> filtros.correspondeAo(item))
                .sorted(ORDENACAO_POR_CONTRATO_E_SERVICO)
                .map(contratoServicoMapper::toResponse)
                .toList();
    }

    public ContratoServicoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(contratoServicoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Relacionamento não encontrado com id: " + id));
    }

    public List<ContratoServicoResponse> listarPorContrato(Long contratoId) {
        return repository.findByContratoId(contratoId).stream()
                .sorted(Comparator.comparing(item -> item.getServico().getNome(), String.CASE_INSENSITIVE_ORDER))
                .map(contratoServicoMapper::toResponse)
                .toList();
    }

    public List<ContratoServicoResponse> listarPorServico(Long servicoId) {
        return repository.findByServicoId(servicoId).stream()
                .sorted(Comparator.comparing(item -> item.getContrato().getNome(), String.CASE_INSENSITIVE_ORDER))
                .map(contratoServicoMapper::toResponse)
                .toList();
    }

    public boolean existeVinculoAtivoParaContrato(Long contratoId) {
        return repository.existsByContratoIdAndAtivoTrue(contratoId);
    }

    public boolean existeVinculoAtivoParaServico(Long servicoId) {
        return repository.existsByServicoIdAndAtivoTrue(servicoId);
    }

    @Transactional
    public ContratoServicoResponse atualizar(Long id, ContratoServicoRequest request) {
        ContratoServico relacionamento = buscarRelacionamento(id);

        boolean mudouVinculo = !relacionamento.getContrato().getId().equals(request.getContratoId())
                || !relacionamento.getServico().getId().equals(request.getServicoId());

        if (mudouVinculo && repository.existsByContratoIdAndServicoId(request.getContratoId(), request.getServicoId())) {
            throw new BusinessException("Esse serviço já está vinculado a esse contrato");
        }

        aplicarVinculo(
                relacionamento,
                buscarContrato(request.getContratoId()),
                buscarServico(request.getServicoId()),
                request
        );

        ContratoServico salvo = repository.save(relacionamento);
        auditoriaService.registrar("CONTRATO_SERVICO", salvo.getId(), "UPDATE_RELACIONAMENTO",
                "Relacionamento atualizado entre contrato " + salvo.getContrato().getNome() + " e serviço " + salvo.getServico().getNome());
        return contratoServicoMapper.toResponse(salvo);
    }

    @Transactional
    public ContratoServicoResponse ativar(Long id) {
        return alterarStatus(id, true);
    }

    @Transactional
    public ContratoServicoResponse inativar(Long id) {
        return alterarStatus(id, false);
    }

    @Transactional
    public void deletar(Long id) {
        repository.delete(buscarRelacionamento(id));
    }

    private ContratoServicoResponse alterarStatus(Long id, boolean ativo) {
        ContratoServico relacionamento = buscarRelacionamento(id);

        relacionamento.setAtivo(ativo);
        return contratoServicoMapper.toResponse(repository.save(relacionamento));
    }

    private ContratoServico buscarRelacionamento(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relacionamento não encontrado com id: " + id));
    }

    private Contrato buscarContrato(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com id: " + id));
    }

    private Servico buscarServico(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com id: " + id));
    }

    private void aplicarVinculo(
            ContratoServico relacionamento,
            Contrato contrato,
            Servico servico,
            ContratoServicoRequest request
    ) {
        relacionamento.setContrato(contrato);
        relacionamento.setServico(servico);
        contratoServicoMapper.aplicarCamposEditaveis(relacionamento, request);
    }

    private List<ContratoServico> buscarRelacionamentos(List<Long> contratoIds, List<Long> servicoIds) {
        boolean temContratos = possuiItens(contratoIds);
        boolean temServicos = possuiItens(servicoIds);

        if (temContratos && temServicos) {
            return repository.findByContratoIdInAndServicoIdIn(contratoIds, servicoIds);
        }
        if (temContratos) {
            return repository.findByContratoIdIn(contratoIds);
        }
        if (temServicos) {
            return repository.findByServicoIdIn(servicoIds);
        }
        return repository.findAll();
    }

    private boolean possuiItens(List<Long> ids) {
        return ids != null && !ids.isEmpty();
    }

    private static boolean contemIgnoreCase(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro.toLowerCase(Locale.ROOT));
    }

    private static final class FiltrosRelacionamento {

        private final String grupo;
        private final String setor;
        private final String contratoNome;
        private final String servicoNome;

        private FiltrosRelacionamento(String grupo, String setor, String contratoNome, String servicoNome) {
            this.grupo = TextoUtils.normalizarFiltro(grupo);
            this.setor = TextoUtils.normalizarFiltro(setor);
            this.contratoNome = TextoUtils.normalizarFiltro(contratoNome);
            this.servicoNome = TextoUtils.normalizarFiltro(servicoNome);
        }

        private boolean correspondeAo(ContratoServico relacionamento) {
            return corresponde(relacionamento.getContrato().getGrupo(), grupo)
                    && corresponde(relacionamento.getSetor(), setor)
                    && corresponde(relacionamento.getContrato().getNome(), contratoNome)
                    && corresponde(relacionamento.getServico().getNome(), servicoNome);
        }

        private boolean corresponde(String valor, String filtro) {
            return filtro == null || contemIgnoreCase(valor, filtro);
        }
    }
}
