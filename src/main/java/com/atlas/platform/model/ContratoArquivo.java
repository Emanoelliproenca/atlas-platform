package com.atlas.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contrato_arquivos")
@Getter
@Setter
@NoArgsConstructor
public class ContratoArquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String tipoConteudo;

    @Column(nullable = false)
    private Long tamanho;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] conteudo;
}
