package com.atlas.platform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "servicos",
        uniqueConstraints = @UniqueConstraint(name = "uk_servicos_nome", columnNames = "nome")
)
@Getter
@Setter
@NoArgsConstructor
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricaoSoftware;

    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "servico")
    private List<ContratoServico> contratos = new ArrayList<>();
}
