package com.atlas.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.platform.dto.ContratoServicoRequest;
import com.atlas.platform.mapper.ContratoServicoMapper;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.ContratoServico;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ContratoRepository;
import com.atlas.platform.repository.ContratoServicoRepository;
import com.atlas.platform.repository.ServicoRepository;
import com.atlas.platform.util.TestEntityFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContratoServicoServiceTest {

    @Mock
    private ContratoServicoRepository repository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Spy
    private ContratoServicoMapper contratoServicoMapper;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ContratoServicoService service;

    @Test
    void listarDeveIgnorarFiltrosEmBrancoEOrdenarPorContratoEServico() {
        when(repository.findAll()).thenReturn(List.of(
                TestEntityFactory.criarRelacionamento("zulu", "Beta", "Financeiro"),
                TestEntityFactory.criarRelacionamento("alpha", "delta", "Operacao"),
                TestEntityFactory.criarRelacionamento("Alpha", "Beta", "Suporte")
        ));

        var resposta = service.listar(null, null, "   ", "   ", "   ", "   ");

        assertThat(resposta)
                .extracting(item -> item.getContratoNome() + "|" + item.getServicoNome())
                .containsExactly("Alpha|Beta", "alpha|delta", "zulu|Beta");
        verify(repository).findAll();
    }

    @Test
    void salvarDeveNormalizarCamposTextuaisOpcionais() {
        Contrato contrato = TestEntityFactory.criarContrato(1L, "Base Operacional", "Gestao");
        Servico servico = TestEntityFactory.criarServico(2L, "Analytics Operacional");

        ContratoServicoRequest request = new ContratoServicoRequest();
        request.setContratoId(1L);
        request.setServicoId(2L);
        request.setVersao("  ");
        request.setObservacao("  ");
        request.setSetor("  ");

        when(repository.existsByContratoIdAndServicoId(1L, 2L)).thenReturn(false);
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(servicoRepository.findById(2L)).thenReturn(Optional.of(servico));
        when(repository.save(any(ContratoServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.salvar(request);

        ArgumentCaptor<ContratoServico> captor = ArgumentCaptor.forClass(ContratoServico.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getVersao()).isNull();
        assertThat(captor.getValue().getObservacao()).isNull();
        assertThat(captor.getValue().getSetor()).isNull();
    }
}
