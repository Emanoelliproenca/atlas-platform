package com.atlas.platform.repository;

import com.atlas.platform.model.Repasse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepasseRepository extends JpaRepository<Repasse, Long> {

    boolean existsByTituloIgnoreCase(String titulo);

    long countByAtivoTrue();

    Optional<Repasse> findByTituloIgnoreCase(String titulo);

    List<Repasse> findByAtivoTrue();
}
