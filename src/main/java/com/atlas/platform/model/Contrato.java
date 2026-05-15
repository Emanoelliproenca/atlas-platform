package com.atlas.platform.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "contratos",
        uniqueConstraints = @UniqueConstraint(name = "uk_contratos_nome", columnNames = "nome")
)
@Getter
@Setter
@NoArgsConstructor
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String grupo;

    private String centralUrl;

    @Column(columnDefinition = "TEXT")
    private String particularidades;

    @Column(columnDefinition = "TEXT")
    private String documentacao;

    private Boolean ativo;

    @OneToMany(mappedBy = "contrato")
    private List<ContratoServico> servicos = new ArrayList<>();
}
