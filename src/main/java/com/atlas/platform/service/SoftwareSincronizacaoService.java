package com.atlas.platform.service;

import com.atlas.platform.config.SoftwareSyncProperties;
import com.atlas.platform.dto.SoftwareSyncResponse;
import com.atlas.platform.exception.BusinessException;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ServicoRepository;
import com.atlas.platform.util.TextoUtils;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class SoftwareSincronizacaoService {

    private final SoftwareSyncProperties properties;
    private final ServicoRepository servicoRepository;
    private final ObjectMapper objectMapper;
    private final AuditoriaService auditoriaService;
    private final HttpClient httpClient;

    public SoftwareSincronizacaoService(
            SoftwareSyncProperties properties,
            ServicoRepository servicoRepository,
            ObjectMapper objectMapper,
            AuditoriaService auditoriaService
    ) {
        this.properties = properties;
        this.servicoRepository = servicoRepository;
        this.objectMapper = objectMapper;
        this.auditoriaService = auditoriaService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Transactional
    public SoftwareSyncResponse sincronizarManifestoExterno() {
        String manifestUrl = properties.getManifestUrl();
        if (manifestUrl == null || manifestUrl.isBlank()) {
            throw new BusinessException("Configure app.software-sync.manifest-url para sincronizar versões com um sistema externo real.");
        }

        List<SoftwareManifestItem> itens = carregarManifesto(manifestUrl.trim());
        int criados = 0;
        int atualizados = 0;

        for (SoftwareManifestItem item : itens) {
            String nome = TextoUtils.normalizarObrigatorio(item.nome());
            Servico servico = servicoRepository.findByNomeIgnoreCase(nome).orElseGet(Servico::new);
            boolean novo = servico.getId() == null;

            servico.setNome(nome);
            servico.setDescricaoSoftware(TextoUtils.normalizarOpcional(item.descricaoSoftware()));
            servico.setVersaoReferencia(TextoUtils.normalizarOpcional(item.versaoReferencia()));
            servico.setLinkSoftware(TextoUtils.normalizarOpcional(item.linkSoftware()));
            servico.setAtivo(item.ativo() == null || item.ativo());
            Servico salvo = servicoRepository.save(servico);

            auditoriaService.registrar("SERVICO", salvo.getId(), novo ? "SINCRONIZACAO_CRIACAO" : "SINCRONIZACAO_ATUALIZACAO",
                    "Serviço sincronizado a partir do manifesto externo.");

            if (novo) {
                criados++;
            } else {
                atualizados++;
            }
        }

        return new SoftwareSyncResponse(itens.size(), criados, atualizados);
    }

    private List<SoftwareManifestItem> carregarManifesto(String manifestUrl) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(manifestUrl))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("Sistema externo retornou status " + response.statusCode() + " ao sincronizar versões.");
            }

            SoftwareManifestItem[] itens = objectMapper.readValue(response.body(), SoftwareManifestItem[].class);
            return Arrays.asList(itens);
        } catch (IOException ex) {
            throw new BusinessException("Não foi possível ler o manifesto externo de versões.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Sincronização de versões interrompida.");
        }
    }

    private record SoftwareManifestItem(
            String nome,
            String descricaoSoftware,
            String versaoReferencia,
            String linkSoftware,
            Boolean ativo
    ) {
    }
}
