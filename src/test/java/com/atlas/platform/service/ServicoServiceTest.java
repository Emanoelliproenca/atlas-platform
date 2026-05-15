package com.atlas.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.platform.dto.ServicoRequest;
import com.atlas.platform.mapper.ServicoMapper;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ServicoRepository;
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
class ServicoServiceTest {

    @Mock
    private ServicoRepository repository;

    @Mock
    private ContratoServicoService contratoServicoService;

    @Spy
    private ServicoMapper servicoMapper;

    @InjectMocks
    private ServicoService service;

    @Test
    void listarDeveIgnorarFiltroEmBrancoERetornarOrdenado() {
        when(repository.findAll()).thenReturn(List.of(
                TestEntityFactory.criarServico("zeta"),
                TestEntityFactory.criarServico("Alpha"),
                TestEntityFactory.criarServico("beta")
        ));

        var resposta = service.listar(null, "   ");

        assertThat(resposta)
                .extracting(item -> item.getNome())
                .containsExactly("Alpha", "beta", "zeta");
        verify(repository).findAll();
        verify(repository, never()).findByNomeContainingIgnoreCase(any());
    }

    @Test
    void salvarDeveNormalizarCamposTextuaisAntesDePersistir() {
        ServicoRequest request = new ServicoRequest();
        request.setNome("  Analytics Operacional  ");
        request.setDescricaoSoftware("   ");
        request.setAtivo(true);

        when(repository.existsByNomeIgnoreCase("Analytics Operacional")).thenReturn(false);
        when(repository.save(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.salvar(request);

        ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getNome()).isEqualTo("Analytics Operacional");
        assertThat(captor.getValue().getDescricaoSoftware()).isNull();
        assertThat(captor.getValue().getAtivo()).isTrue();
        verify(repository).existsByNomeIgnoreCase(eq("Analytics Operacional"));
    }
}
