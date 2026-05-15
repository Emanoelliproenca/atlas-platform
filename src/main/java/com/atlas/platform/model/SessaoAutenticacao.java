package com.atlas.platform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "sessoes_autenticacao",
        indexes = {
                @Index(name = "idx_sessoes_token_hash", columnList = "tokenHash", unique = true),
                @Index(name = "idx_sessoes_expiracao", columnList = "expiraEm")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class SessaoAutenticacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String roles;

    @Column(nullable = false)
    private Instant criadaEm;

    @Column(nullable = false)
    private Instant expiraEm;

    @Column(nullable = false)
    private boolean invalida;
}
