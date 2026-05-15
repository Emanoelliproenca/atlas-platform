package com.atlas.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.platform.dto.ContratoRequest;
import com.atlas.platform.mapper.ContratoMapper;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.repository.ContratoRepository;
import com.atlas.platform.util.TestEntityFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

    @Mock
    private ContratoRepository repository;

    @Mock
    private ContratoServicoService contratoServicoService;

    @Spy
    private ContratoMapper contratoMapper;

    @InjectMocks
    private ContratoService service;

    @Test
    void listarDeveIgnorarFiltrosEmBrancoERetornarOrdenadoPorNome() {
        when(repository.findAll()).thenReturn(List.of(
                TestEntityFactory.criarContrato("zulu", "Operacao"),
                TestEntityFactory.criarContrato("Alpha", "Gestao"),
                TestEntityFactory.criarContrato("beta", "Suporte")
        ));

        var resposta = service.listar(null, "   ", "   ");

        assertThat(resposta)
                .extracting(item -> item.getNome())
                .containsExactly("Alpha", "beta", "zulu");
        verify(repository).findAll();
        verify(repository, never()).findByGrupo(any());
        verify(repository, never()).findByNomeContainingIgnoreCase(any());
    }

    @Test
    void salvarDeveNormalizarCamposObrigatoriosEOpcionaisAntesDePersistir() {
        ContratoRequest request = new ContratoRequest();
        request.setNome("  Portal Base  ");
        request.setGrupo("  Operacao  ");
        request.setCentralUrl("  https://central-demo.example.invalid  ");
        request.setParticularidades("   ");
        request.setDocumentacao("  wiki demo  ");
        request.setAtivo(true);

        when(repository.existsByNomeIgnoreCase("Portal Base")).thenReturn(false);
        when(repository.save(any(Contrato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.salvar(request);

        ArgumentCaptor<Contrato> captor = ArgumentCaptor.forClass(Contrato.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getNome()).isEqualTo("Portal Base");
        assertThat(captor.getValue().getGrupo()).isEqualTo("Operacao");
        assertThat(captor.getValue().getCentralUrl()).isEqualTo("https://central-demo.example.invalid");
        assertThat(captor.getValue().getParticularidades()).isNull();
        assertThat(captor.getValue().getDocumentacao()).isEqualTo("wiki demo");
        assertThat(captor.getValue().getAtivo()).isTrue();
        verify(repository).existsByNomeIgnoreCase(eq("Portal Base"));
    }
}
