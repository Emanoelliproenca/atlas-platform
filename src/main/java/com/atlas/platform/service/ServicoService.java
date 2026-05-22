package com.atlas.platform.service;

import com.atlas.platform.dto.ServicoDetalheResponse;
import com.atlas.platform.dto.ServicoRequest;
import com.atlas.platform.dto.ServicoResponse;
import com.atlas.platform.exception.BusinessException;
import com.atlas.platform.exception.ResourceNotFoundException;
import com.atlas.platform.mapper.ServicoMapper;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ServicoRepository;
import com.atlas.platform.util.TextoUtils;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServicoService {

    private static final Comparator<Servico> ORDENACAO_POR_NOME =
            Comparator.comparing(Servico::getNome, String.CASE_INSENSITIVE_ORDER);
    private static final String MENSAGEM_SERVICO_DUPLICADO = "Já existe um serviço com esse nome";

    private final ServicoRepository repository;
    private final ContratoServicoService contratoServicoService;
    private final ServicoMapper servicoMapper;
    private final AuditoriaService auditoriaService;

    public ServicoService(
            ServicoRepository repository,
            ContratoServicoService contratoServicoService,
            ServicoMapper servicoMapper,
            AuditoriaService auditoriaService
    ) {
        this.repository = repository;
        this.contratoServicoService = contratoServicoService;
        this.servicoMapper = servicoMapper;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ServicoResponse salvar(ServicoRequest request) {
        validarNomeDisponivel(request.getNome(), null);

        Servico servico = new Servico();
        servicoMapper.aplicarRequest(servico, request);
        Servico salvo = repository.save(servico);
        auditoriaService.registrar("SERVICO", salvo.getId(), "CREATE_SERVICO", "Serviço criado: " + salvo.getNome());
        return servicoMapper.toResponse(salvo);
    }

    public List<ServicoResponse> listar(Boolean ativo, String nome) {
        FiltrosServico filtros = new FiltrosServico(ativo, nome);

        return buscarServicos(filtros).stream()
                .sorted(ORDENACAO_POR_NOME)
                .map(servicoMapper::toResponse)
                .toList();
    }

    public ServicoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(servicoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com id: " + id));
    }

    public ServicoDetalheResponse buscarDetalhePorId(Long id) {
        ServicoResponse servico = buscarPorId(id);
        return new ServicoDetalheResponse(
                servico,
                contratoServicoService.listarPorServico(id)
        );
    }

    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        Servico servico = buscarEntidadePorId(id);
        validarNomeDisponivel(request.getNome(), id);
        servicoMapper.aplicarRequest(servico, request);
        Servico salvo = repository.save(servico);
        auditoriaService.registrar("SERVICO", salvo.getId(), "UPDATE_SERVICO", "Serviço atualizado: " + salvo.getNome());
        return servicoMapper.toResponse(salvo);
    }

    @Transactional
    public void deletar(Long id) {
        Servico servico = buscarEntidadePorId(id);
        validarInativacaoPermitida(id);
        servico.setAtivo(false);
        Servico salvo = repository.save(servico);
        auditoriaService.registrar("SERVICO", salvo.getId(), "INACTIVATE_SERVICO", "Serviço inativado: " + salvo.getNome());
    }

    @Transactional
    public ServicoResponse ativar(Long id) {
        return alterarStatus(id, true);
    }

    @Transactional
    public ServicoResponse inativar(Long id) {
        return alterarStatus(id, false);
    }

    private Servico buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com id: " + id));
    }

    private List<Servico> buscarServicos(FiltrosServico filtros) {
        if (filtros.temAtivo() && filtros.temNome()) {
            return repository.findByAtivoAndNomeContainingIgnoreCase(filtros.ativo(), filtros.nome());
        }
        if (filtros.temAtivo()) {
            return repository.findByAtivo(filtros.ativo());
        }
        if (filtros.temNome()) {
            return repository.findByNomeContainingIgnoreCase(filtros.nome());
        }
        return repository.findAll();
    }

    private void validarNomeDisponivel(String nome, Long servicoIdAtual) {
        String nomeNormalizado = TextoUtils.normalizarObrigatorio(nome);
        boolean nomeJaExiste = servicoIdAtual == null
                ? repository.existsByNomeIgnoreCase(nomeNormalizado)
                : repository.existsByNomeIgnoreCaseAndIdNot(nomeNormalizado, servicoIdAtual);

        if (nomeJaExiste) {
            throw new BusinessException(MENSAGEM_SERVICO_DUPLICADO);
        }
    }

    private ServicoResponse alterarStatus(Long id, boolean ativo) {
        Servico servico = buscarEntidadePorId(id);
        if (!ativo) {
            validarInativacaoPermitida(id);
        }
        servico.setAtivo(ativo);
        Servico salvo = repository.save(servico);
        auditoriaService.registrar("SERVICO", salvo.getId(), ativo ? "ACTIVATE_SERVICO" : "INACTIVATE_SERVICO",
                (ativo ? "Serviço ativado: " : "Serviço inativado: ") + salvo.getNome());
        return servicoMapper.toResponse(salvo);
    }

    private void validarInativacaoPermitida(Long id) {
        if (contratoServicoService.existeVinculoAtivoParaServico(id)) {
            throw new BusinessException("Não é possível inativar o serviço porque existem vínculos ativos com contratos.");
        }
    }

    private record FiltrosServico(Boolean ativo, String nome) {

        private FiltrosServico(Boolean ativo, String nome) {
            this.ativo = ativo;
            this.nome = TextoUtils.normalizarFiltro(nome);
        }

        private boolean temAtivo() {
            return ativo != null;
        }

        private boolean temNome() {
            return nome != null;
        }
    }
}
