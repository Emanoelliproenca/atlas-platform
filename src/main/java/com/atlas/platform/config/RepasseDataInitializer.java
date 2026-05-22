package com.atlas.platform.config;

import com.atlas.platform.model.Repasse;
import com.atlas.platform.repository.RepasseRepository;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"demo", "test", "seed"})
public class RepasseDataInitializer {

    @Bean
    public CommandLineRunner inicializarRepasses(RepasseRepository repasseRepository) {
        return args -> {
            obterOuCriarRepasse(repasseRepository,
                    "Onboarding de nova operação",
                    "Onboarding de nova organizacao",
                    "Implantação",
                    "Registrar escopo, responsáveis, marcos de implantação e riscos simulados antes de encerrar o handoff operacional.",
                    "Alta"
            );
            obterOuCriarRepasse(repasseRepository,
                    "Checklist de release demo",
                    null,
                    "Produto",
                    "Validar versão fictícia, serviços impactados no ambiente demo e critérios simulados de comunicação e reversão.",
                    "Média"
            );
            obterOuCriarRepasse(repasseRepository,
                    "Mensagem padrão para suporte",
                    "Mensagem padrao para suporte",
                    "Comunicação",
                    "Manter modelos de resposta, orientações de triagem e instruções para alinhamento rápido com a operação demonstrativa.",
                    "Baixa"
            );
        };
    }

    private void obterOuCriarRepasse(
            RepasseRepository repasseRepository,
            String titulo,
            String tituloLegado,
            String categoria,
            String conteudo,
            String prioridade
    ) {
        Repasse repasse = buscarRepasseExistente(repasseRepository, titulo, tituloLegado).orElseGet(Repasse::new);
        repasse.setTitulo(titulo);
        repasse.setCategoria(categoria);
        repasse.setConteudo(conteudo);
        repasse.setPrioridade(prioridade);
        repasse.setAtivo(true);
        repasse.setFixado("Alta".equalsIgnoreCase(prioridade));
        repasse.setAutor("ATLAS");
        repasse.setAnexoNome(null);
        if (repasse.getCriadoEm() == null) {
            repasse.setCriadoEm(java.time.Instant.now());
        }
        repasseRepository.save(repasse);
    }

    private Optional<Repasse> buscarRepasseExistente(
            RepasseRepository repasseRepository,
            String titulo,
            String tituloLegado
    ) {
        Optional<Repasse> repasse = repasseRepository.findByTituloIgnoreCase(titulo);
        if (repasse.isPresent() || tituloLegado == null || tituloLegado.isBlank()) {
            return repasse;
        }

        return repasseRepository.findByTituloIgnoreCase(tituloLegado);
    }
}
