package com.atlas.platform.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportacaoPlanilhaResponse {

    private int contratosCriados;
    private int contratosAtualizados;
    private int servicosCriados;
    private int servicosAtualizados;
    private int relacionamentosCriados;
    private int relacionamentosAtualizados;
    private int relacionamentosIgnorados;
    private List<String> avisos = new ArrayList<>();

    public ImportacaoPlanilhaResponse() {
    }

    public int getContratosCriados() {
        return contratosCriados;
    }

    public void setContratosCriados(int contratosCriados) {
        this.contratosCriados = contratosCriados;
    }

    public int getContratosAtualizados() {
        return contratosAtualizados;
    }

    public void setContratosAtualizados(int contratosAtualizados) {
        this.contratosAtualizados = contratosAtualizados;
    }

    public int getServicosCriados() {
        return servicosCriados;
    }

    public void setServicosCriados(int servicosCriados) {
        this.servicosCriados = servicosCriados;
    }

    public int getServicosAtualizados() {
        return servicosAtualizados;
    }

    public void setServicosAtualizados(int servicosAtualizados) {
        this.servicosAtualizados = servicosAtualizados;
    }

    public int getRelacionamentosCriados() {
        return relacionamentosCriados;
    }

    public void setRelacionamentosCriados(int relacionamentosCriados) {
        this.relacionamentosCriados = relacionamentosCriados;
    }

    public int getRelacionamentosAtualizados() {
        return relacionamentosAtualizados;
    }

    public void setRelacionamentosAtualizados(int relacionamentosAtualizados) {
        this.relacionamentosAtualizados = relacionamentosAtualizados;
    }

    public int getRelacionamentosIgnorados() {
        return relacionamentosIgnorados;
    }

    public void setRelacionamentosIgnorados(int relacionamentosIgnorados) {
        this.relacionamentosIgnorados = relacionamentosIgnorados;
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public void setAvisos(List<String> avisos) {
        this.avisos = avisos;
    }

    public void adicionarAviso(String aviso) {
        this.avisos.add(aviso);
    }

    public void incrementarContratosCriados() {
        this.contratosCriados++;
    }

    public void incrementarContratosAtualizados() {
        this.contratosAtualizados++;
    }

    public void incrementarServicosCriados() {
        this.servicosCriados++;
    }

    public void incrementarServicosAtualizados() {
        this.servicosAtualizados++;
    }

    public void incrementarRelacionamentosCriados() {
        this.relacionamentosCriados++;
    }

    public void incrementarRelacionamentosAtualizados() {
        this.relacionamentosAtualizados++;
    }

    public void incrementarRelacionamentosIgnorados() {
        this.relacionamentosIgnorados++;
    }

    public void registrarRelacionamentoIgnorado(String aviso) {
        incrementarRelacionamentosIgnorados();
        adicionarAviso(aviso);
    }
}
