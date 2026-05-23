package com.atlas.platform.repository;

import com.atlas.platform.model.SoftwareArtefato;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoftwareArtefatoRepository extends JpaRepository<SoftwareArtefato, Long> {

    List<SoftwareArtefato> findByServicoIdOrderByCriadoEmDesc(Long servicoId);

    Optional<SoftwareArtefato> findFirstByServicoIdAndAtivoTrueOrderByCriadoEmDesc(Long servicoId);

    boolean existsByServicoIdAndAtivoTrue(Long servicoId);
}
