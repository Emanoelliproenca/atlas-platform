package com.atlas.platform.repository;

import com.atlas.platform.model.AuditoriaEvento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento, Long> {

    List<AuditoriaEvento> findTop100ByOrderByTimestampDesc();
}
