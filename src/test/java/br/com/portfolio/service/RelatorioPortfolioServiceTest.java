package br.com.portfolio.service;

import br.com.portfolio.domain.enums.StatusProjeto;
import br.com.portfolio.dto.RelatorioPortfolioResponse;
import br.com.portfolio.repository.ProjetoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioPortfolioServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @InjectMocks
    private RelatorioPortfolioService relatorioPortfolioService;

    @Test
    void geraRelatorio() {
        java.util.List<Object[]> linhas = new java.util.ArrayList<>();
        linhas.add(new Object[]{StatusProjeto.EM_ANALISE, 2L, new BigDecimal("1000")});
        when(projetoRepository.agregarPorStatus()).thenReturn(linhas);
        when(projetoRepository.mediaDuracaoDiasProjetosEncerrados()).thenReturn(30.5);
        when(projetoRepository.countMembrosUnicosAlocados()).thenReturn(5L);

        RelatorioPortfolioResponse r = relatorioPortfolioService.gerar();

        assertThat(r.porStatus()).hasSize(1);
        assertThat(r.porStatus().getFirst().quantidadeProjetos()).isEqualTo(2L);
        assertThat(r.mediaDuracaoDiasProjetosEncerrados()).isEqualTo(30.5);
        assertThat(r.totalMembrosUnicosAlocados()).isEqualTo(5L);
    }
}
