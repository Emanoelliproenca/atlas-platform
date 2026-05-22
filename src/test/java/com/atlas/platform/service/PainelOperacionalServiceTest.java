package com.atlas.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.platform.dto.ContratoResponse;
import com.atlas.platform.dto.ContratoServicoResponse;
import com.atlas.platform.dto.ServicoResponse;
import com.atlas.platform.mapper.PainelOperacionalMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PainelOperacionalServiceTest {

    @Mock
    private ContratoService contratoService;

    @Mock
    private ServicoService servicoService;

    @Mock
    private ContratoServicoService contratoServicoService;

    @Spy
    private PainelOperacionalMapper painelOperacionalMapper;

    @InjectMocks
    private PainelOperacionalService service;

    @Test
    void consultarDeveMontarResumoFiltrosELinhasOrdenadas() {
        when(contratoServicoService.listar(List.of(1L), List.of(20L), "Grupo B", "Suporte", "", "")).thenReturn(List.of(
                new ContratoServicoResponse(300L, 2L, "zulu", 20L, "Beta", "2.0", "Obs 2", "Suporte", true),
                new ContratoServicoResponse(100L, 1L, "Alpha", 10L, "Delta", "1.0", "Obs 1", "Operacao", true),
                new ContratoServicoResponse(200L, 1L, "Alpha", 20L, "Beta", "", "", "Suporte", true)
        ));
        when(contratoService.listar(true, null, null)).thenReturn(List.of(
                new ContratoResponse(2L, "zulu", "Grupo B", "https://zulu-demo.example.invalid", null, null, true),
                new ContratoResponse(1L, "Alpha", "Grupo A", "https://alpha-demo.example.invalid", null, null, true)
        ));
        when(servicoService.listar(true, null)).thenReturn(List.of(
                new ServicoResponse(20L, "Beta", "Servico beta", null, null, true),
                new ServicoResponse(10L, "Delta", "Servico delta", null, null, true)
        ));

        var resposta = service.consultar(List.of(1L), List.of(20L), "Grupo B", "Suporte", "", "");

        assertThat(resposta.getLinhas())
                .extracting(item -> item.getContratoNome() + "|" + item.getServicoNome())
                .containsExactly("Alpha|Beta", "Alpha|Delta", "zulu|Beta");

        assertThat(resposta.getFiltros().getContratos())
                .extracting(item -> item.getNome())
                .containsExactly("Alpha", "zulu");

        assertThat(resposta.getFiltros().getServicos())
                .extracting(item -> item.getNome())
                .containsExactly("Beta", "Delta");

        assertThat(resposta.getFiltros().getGrupos()).containsExactly("Grupo A", "Grupo B");
        assertThat(resposta.getFiltros().getSetores()).containsExactly("Operacao", "Suporte");

        assertThat(resposta.getResumo().getTotalRelacionamentos()).isEqualTo(3);
        assertThat(resposta.getResumo().getTotalContratos()).isEqualTo(2);
        assertThat(resposta.getResumo().getTotalServicos()).isEqualTo(2);
        assertThat(resposta.getResumo().getTotalGrupos()).isEqualTo(2);
        assertThat(resposta.getResumo().getTotalSetores()).isEqualTo(2);

        assertThat(resposta.getLinhas().get(0).getGrupo()).isEqualTo("Grupo A");
        assertThat(resposta.getLinhas().get(0).getCentralUrl()).isEqualTo("https://alpha-demo.example.invalid");
        assertThat(resposta.getLinhas().get(2).getGrupo()).isEqualTo("Grupo B");

        verify(contratoServicoService).listar(List.of(1L), List.of(20L), "Grupo B", "Suporte", "", "");
        verify(contratoService).listar(true, null, null);
        verify(servicoService).listar(true, null);
    }
}
