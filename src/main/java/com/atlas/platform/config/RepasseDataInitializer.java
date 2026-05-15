package com.atlas.platform.config;

import com.atlas.platform.model.Repasse;
import com.atlas.platform.repository.RepasseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepasseDataInitializer {

    @Bean
    public CommandLineRunner inicializarRepasses(RepasseRepository repasseRepository) {
        return args -> {
            if (repasseRepository.count() > 0) {
                return;
            }

            repasseRepository.save(criarRepasse(
                    "Onboarding de nova organizacao",
                    "Implantacao",
                    "Registrar escopo contratado, responsaveis, marcos de implantacao e riscos antes de encerrar o handoff.",
                    "Alta"
            ));
            repasseRepository.save(criarRepasse(
                    "Checklist de release demo",
                    "Produto",
                    "Registrar versao ficticia, servicos impactados no ambiente demo e criterios simulados de rollback.",
                    "Media"
            ));
            repasseRepository.save(criarRepasse(
                    "Mensagem padrao para suporte",
                    "Comunicacao",
                    "Manter modelos de e-mail, texto de retorno e instrucoes para alinhamento rapido com organizacao e operacao.",
                    "Baixa"
            ));
        };
    }

    private Repasse criarRepasse(String titulo, String categoria, String conteudo, String prioridade) {
        Repasse repasse = new Repasse();
        repasse.setTitulo(titulo);
        repasse.setCategoria(categoria);
        repasse.setConteudo(conteudo);
        repasse.setPrioridade(prioridade);
        repasse.setAtivo(true);
        return repasse;
    }
}
