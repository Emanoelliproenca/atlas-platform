package com.atlas.platform.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardMetricsResponse {

    private long totalContratos;
    private long contratosAtivos;
    private long contratosInativos;
    private long totalServicos;
    private long servicosAtivos;
    private long servicosInativos;
    private long totalRelacionamentos;
    private long relacionamentosAtivos;
    private long repassesAtivos;
    private long softwaresAtivos;
    private long softwaresDesatualizados;
    private double percentualContratosComDocumentacao;
    private double percentualContratosComServicos;
    private List<DistribuicaoItem> contratosPorGrupo;
    private List<DistribuicaoItem> servicosPorSetor;
    private List<ContratoRecente> ultimosContratos;
    private List<RepasseRecente> ultimosRepasses;

    @Getter
    @AllArgsConstructor
    public static class DistribuicaoItem {
        private String nome;
        private long total;
    }

    @Getter
    @AllArgsConstructor
    public static class ContratoRecente {
        private Long id;
        private String nome;
        private String grupo;
        private Boolean ativo;
    }

    @Getter
    @AllArgsConstructor
    public static class RepasseRecente {
        private Long id;
        private String titulo;
        private String categoria;
        private String prioridade;
        private Instant criadoEm;
    }
}
