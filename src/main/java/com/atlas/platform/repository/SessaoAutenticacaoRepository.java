package com.atlas.platform.repository;

import com.atlas.platform.model.SessaoAutenticacao;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessaoAutenticacaoRepository extends JpaRepository<SessaoAutenticacao, Long> {

    Optional<SessaoAutenticacao> findByTokenHashAndInvalidaFalse(String tokenHash);

    void deleteByExpiraEmBeforeOrInvalidaTrue(Instant instante);
}
