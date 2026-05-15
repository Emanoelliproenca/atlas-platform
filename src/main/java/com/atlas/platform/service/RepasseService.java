package com.atlas.platform.service;

import com.atlas.platform.dto.RepasseResponse;
import com.atlas.platform.model.Repasse;
import com.atlas.platform.repository.RepasseRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RepasseService {

    private static final Comparator<Repasse> ORDENACAO_REPASSES = Comparator
            .comparingInt((Repasse repasse) -> prioridadePeso(repasse.getPrioridade()))
            .thenComparing(Repasse::getTitulo, String.CASE_INSENSITIVE_ORDER);

    private final RepasseRepository repository;

    public RepasseService(RepasseRepository repository) {
        this.repository = repository;
    }

    public List<RepasseResponse> listarAtivos() {
        return repository.findByAtivoTrue().stream()
                .sorted(ORDENACAO_REPASSES)
                .map(this::toResponse)
                .toList();
    }

    private static int prioridadePeso(String prioridade) {
        if (prioridade == null) {
            return Integer.MAX_VALUE;
        }

        return switch (prioridade.trim().toLowerCase()) {
            case "alta" -> 0;
            case "media" -> 1;
            case "baixa" -> 2;
            default -> 3;
        };
    }

    private RepasseResponse toResponse(Repasse repasse) {
        return new RepasseResponse(
                repasse.getId(),
                repasse.getTitulo(),
                repasse.getCategoria(),
                repasse.getConteudo(),
                repasse.getPrioridade(),
                repasse.getAtivo()
        );
    }
}
