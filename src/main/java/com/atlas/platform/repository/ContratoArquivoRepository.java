package com.atlas.platform.repository;

import com.atlas.platform.model.ContratoArquivo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoArquivoRepository extends JpaRepository<ContratoArquivo, Long> {

    List<ContratoArquivo> findByContratoIdOrderByCriadoEmDesc(Long contratoId);
}
