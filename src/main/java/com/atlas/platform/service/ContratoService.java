package com.atlas.platform.service;

import com.atlas.platform.dto.ContratoDetalheResponse;
import com.atlas.platform.dto.ContratoRequest;
import com.atlas.platform.dto.ContratoResponse;
import com.atlas.platform.exception.BusinessException;
import com.atlas.platform.exception.ResourceNotFoundException;
import com.atlas.platform.mapper.ContratoMapper;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.repository.ContratoRepository;
import com.atlas.platform.util.TextoUtils;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ContratoService {

    private static final Comparator<Contrato> ORDENACAO_POR_NOME =
            Comparator.comparing(Contrato::getNome, String.CASE_INSENSITIVE_ORDER);
    private static final String MENSAGEM_CONTRATO_DUPLICADO = "Já existe um contrato com esse nome";

    private final ContratoRepository repository;
    private final ContratoServicoService contratoServicoService;
    private final ContratoArquivoService contratoArquivoService;
    private final ContratoMapper contratoMapper;

    public ContratoService(
            ContratoRepository repository,
            ContratoServicoService contratoServicoService,
            ContratoArquivoService contratoArquivoService,
            ContratoMapper contratoMapper
    ) {
        this.repository = repository;
        this.contratoServicoService = contratoServicoService;
        this.contratoArquivoService = contratoArquivoService;
        this.contratoMapper = contratoMapper;
    }

    @Transactional
    public ContratoResponse salvar(ContratoRequest request) {
        validarNomeDisponivel(request.getNome(), null);

        Contrato contrato = new Contrato();
        contratoMapper.aplicarRequest(contrato, request);
        return contratoMapper.toResponse(repository.save(contrato));
    }

    public List<ContratoResponse> listar(Boolean ativo, String grupo, String nome) {
        FiltrosContrato filtros = new FiltrosContrato(ativo, grupo, nome);

        return buscarContratos(filtros).stream()
                .sorted(ORDENACAO_POR_NOME)
                .map(contratoMapper::toResponse)
                .toList();
    }

    public ContratoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(contratoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com id: " + id));
    }

    public ContratoDetalheResponse buscarDetalhePorId(Long id) {
        ContratoResponse contrato = buscarPorId(id);
        return new ContratoDetalheResponse(
                contrato,
                contratoServicoService.listarPorContrato(id),
                contratoArquivoService.listarPorContrato(id)
        );
    }

    @Transactional
    public ContratoResponse atualizar(Long id, ContratoRequest request) {
        Contrato contrato = buscarEntidadePorId(id);
        validarNomeDisponivel(request.getNome(), id);
        contratoMapper.aplicarRequest(contrato, request);
        return contratoMapper.toResponse(repository.save(contrato));
    }

    @Transactional
    public void deletar(Long id) {
        Contrato contrato = buscarEntidadePorId(id);
        contrato.setAtivo(false);
        repository.save(contrato);
    }

    @Transactional
    public ContratoResponse ativar(Long id) {
        return alterarStatus(id, true);
    }

    @Transactional
    public ContratoResponse inativar(Long id) {
        return alterarStatus(id, false);
    }

    private Contrato buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com id: " + id));
    }

    private List<Contrato> buscarContratos(FiltrosContrato filtros) {
        if (filtros.temAtivo() && filtros.temGrupo() && filtros.temNome()) {
            return repository.findByAtivoAndGrupoAndNomeContainingIgnoreCase(filtros.ativo(), filtros.grupo(), filtros.nome());
        }
        if (filtros.temAtivo() && filtros.temGrupo()) {
            return repository.findByAtivoAndGrupo(filtros.ativo(), filtros.grupo());
        }
        if (filtros.temAtivo() && filtros.temNome()) {
            return repository.findByAtivoAndNomeContainingIgnoreCase(filtros.ativo(), filtros.nome());
        }
        if (filtros.temGrupo() && filtros.temNome()) {
            return repository.findByGrupoAndNomeContainingIgnoreCase(filtros.grupo(), filtros.nome());
        }
        if (filtros.temAtivo()) {
            return repository.findByAtivo(filtros.ativo());
        }
        if (filtros.temGrupo()) {
            return repository.findByGrupo(filtros.grupo());
        }
        if (filtros.temNome()) {
            return repository.findByNomeContainingIgnoreCase(filtros.nome());
        }
        return repository.findAll();
    }

    private void validarNomeDisponivel(String nome, Long contratoIdAtual) {
        String nomeNormalizado = TextoUtils.normalizarObrigatorio(nome);
        boolean nomeJaExiste = contratoIdAtual == null
                ? repository.existsByNomeIgnoreCase(nomeNormalizado)
                : repository.existsByNomeIgnoreCaseAndIdNot(nomeNormalizado, contratoIdAtual);

        if (nomeJaExiste) {
            throw new BusinessException(MENSAGEM_CONTRATO_DUPLICADO);
        }
    }

    private ContratoResponse alterarStatus(Long id, boolean ativo) {
        Contrato contrato = buscarEntidadePorId(id);
        contrato.setAtivo(ativo);
        return contratoMapper.toResponse(repository.save(contrato));
    }

    private record FiltrosContrato(Boolean ativo, String grupo, String nome) {

        private FiltrosContrato(Boolean ativo, String grupo, String nome) {
            this.ativo = ativo;
            this.grupo = TextoUtils.normalizarFiltro(grupo);
            this.nome = TextoUtils.normalizarFiltro(nome);
        }

        private boolean temAtivo() {
            return ativo != null;
        }

        private boolean temGrupo() {
            return grupo != null;
        }

        private boolean temNome() {
            return nome != null;
        }
    }
}
