package com.atlas.platform.repository;

import com.atlas.platform.model.Repasse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepasseRepository extends JpaRepository<Repasse, Long> {

    List<Repasse> findByAtivoTrue();
}
