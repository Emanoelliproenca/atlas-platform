package com.atlas.platform.util;

import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.ContratoServico;
import com.atlas.platform.model.Servico;

public final class TestEntityFactory {

    private TestEntityFactory() {
    }

    public static Contrato criarContrato(String nome, String grupo) {
        return criarContrato(null, nome, grupo);
    }

    public static Contrato criarContrato(Long id, String nome, String grupo) {
        Contrato contrato = new Contrato();
        contrato.setNome(nome);
        contrato.setGrupo(grupo);
        contrato.setAtivo(true);
        definirCampoIdSeNecessario(contrato, id);
        return contrato;
    }

    public static Servico criarServico(String nome) {
        return criarServico(null, nome);
    }

    public static Servico criarServico(Long id, String nome) {
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setAtivo(true);
        definirCampoIdSeNecessario(servico, id);
        return servico;
    }

    public static ContratoServico criarRelacionamento(String contratoNome, String servicoNome, String setor) {
        ContratoServico relacionamento = new ContratoServico();
        relacionamento.setContrato(criarContrato(contratoNome, "Grupo"));
        relacionamento.setServico(criarServico(servicoNome));
        relacionamento.setSetor(setor);
        relacionamento.setAtivo(true);
        return relacionamento;
    }

    private static void definirCampoIdSeNecessario(Object entidade, Long id) {
        if (id == null) {
            return;
        }

        try {
            var field = entidade.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entidade, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
