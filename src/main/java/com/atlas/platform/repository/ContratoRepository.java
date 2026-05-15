package com.atlas.platform.repository;

import com.atlas.platform.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<Contrato> findByNomeIgnoreCase(String nome);

    List<Contrato> findByAtivo(Boolean ativo);

    List<Contrato> findByGrupo(String grupo);

    List<Contrato> findByNomeContainingIgnoreCase(String nome);

    List<Contrato> findByAtivoAndGrupo(Boolean ativo, String grupo);

    List<Contrato> findByAtivoAndNomeContainingIgnoreCase(Boolean ativo, String nome);

    List<Contrato> findByGrupoAndNomeContainingIgnoreCase(String grupo, String nome);

    List<Contrato> findByAtivoAndGrupoAndNomeContainingIgnoreCase(Boolean ativo, String grupo, String nome);
}
