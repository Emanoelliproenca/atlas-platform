package com.atlas.platform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "contrato_servicos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_contrato_servico",
                columnNames = {"contrato_id", "servico_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ContratoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @ManyToOne(optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    private String versao;

    @Column(name = "observacao")
    private String observacao;

    private String setor;

    @Column(nullable = false)
    private Boolean ativo = true;
}
