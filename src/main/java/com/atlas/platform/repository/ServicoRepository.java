package com.atlas.platform.repository;

import com.atlas.platform.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<Servico> findByNomeIgnoreCase(String nome);

    List<Servico> findByAtivo(Boolean ativo);

    List<Servico> findByNomeContainingIgnoreCase(String nome);

    List<Servico> findByAtivoAndNomeContainingIgnoreCase(Boolean ativo, String nome);
}
