package com.atlas.platform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entidade;

    @Column(nullable = false)
    private Long entidadeId;

    @Column(nullable = false)
    private String acao;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false, length = 1000)
    private String detalhes;

    private String ip;

    @Column(nullable = false, name = "timestamp")
    private Instant timestamp;
}
