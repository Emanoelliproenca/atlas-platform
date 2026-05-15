package com.atlas.platform.repository;

import com.atlas.platform.model.ContratoServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContratoServicoRepository extends JpaRepository<ContratoServico, Long> {

    boolean existsByContratoIdAndServicoId(Long contratoId, Long servicoId);

    Optional<ContratoServico> findByContratoIdAndServicoId(Long contratoId, Long servicoId);

    List<ContratoServico> findByAtivo(Boolean ativo);

    List<ContratoServico> findByContratoId(Long contratoId);

    List<ContratoServico> findByServicoId(Long servicoId);

    List<ContratoServico> findByContratoIdIn(List<Long> contratoIds);

    List<ContratoServico> findByServicoIdIn(List<Long> servicoIds);

    List<ContratoServico> findByContratoIdInAndServicoIdIn(List<Long> contratoIds, List<Long> servicoIds);
}
