package br.com.portfolio.service;

import br.com.portfolio.domain.entity.Membro;
import br.com.portfolio.domain.entity.Projeto;
import br.com.portfolio.domain.enums.AtribuicaoMembro;
import br.com.portfolio.domain.enums.StatusProjeto;
import br.com.portfolio.dto.ProjetoRequest;
import br.com.portfolio.dto.ProjetoResponse;
import br.com.portfolio.dto.ProjetoUpdateRequest;
import br.com.portfolio.exception.NegocioException;
import br.com.portfolio.exception.RecursoNaoEncontradoException;
import br.com.portfolio.repository.ProjetoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;
    @Mock
    private MembroExternoService membroExternoService;
    @Mock
    private ProjetoMapper projetoMapper;

    private ProjetoService projetoService;

    private Membro gerente;
    private Membro func1;

    @BeforeEach
    void setUp() {
        projetoService = new ProjetoService(
                projetoRepository,
                membroExternoService,
                projetoMapper,
                new StatusProjetoValidator()
        );
        gerente = Membro.builder().id(1L).nome("G").atribuicao(AtribuicaoMembro.GERENTE).build();
        func1 = Membro.builder().id(2L).nome("F").atribuicao(AtribuicaoMembro.FUNCIONARIO).build();
    }

    @Test
    @DisplayName("Exclusão bloqueada para iniciado, em andamento e encerrado")
    void excluirBloqueado() {
        for (StatusProjeto s : List.of(StatusProjeto.INICIADO, StatusProjeto.EM_ANDAMENTO, StatusProjeto.ENCERRADO)) {
            Projeto p = Projeto.builder().id(10L).status(s).build();
            when(projetoRepository.findById(10L)).thenReturn(Optional.of(p));
            assertThatThrownBy(() -> projetoService.excluir(10L)).isInstanceOf(NegocioException.class);
        }
    }

    @Test
    @DisplayName("Exclusão permitida em análise")
    void excluirPermitido() {
        Projeto p = Projeto.builder().id(10L).status(StatusProjeto.EM_ANALISE).build();
        when(projetoRepository.findById(10L)).thenReturn(Optional.of(p));
        projetoService.excluir(10L);
        verify(projetoRepository).delete(p);
    }

    @Test
    @DisplayName("Projeto inexistente ao excluir")
    void excluirNaoEncontrado() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projetoService.excluir(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Criação valida limite de 3 projetos ativos por membro")
    void limiteTresProjetos() {
        when(membroExternoService.obterEntidade(1L)).thenReturn(gerente);
        when(membroExternoService.obterEntidade(2L)).thenReturn(func1);
        when(projetoRepository.countProjetosAtivosPorMembro(2L, null)).thenReturn(3L);

        var req = new ProjetoRequest(
                "P",
                LocalDate.now(),
                LocalDate.now().plusMonths(2),
                null,
                new BigDecimal("10000"),
                "d",
                1L,
                List.of(2L)
        );

        assertThatThrownBy(() -> projetoService.criar(req)).isInstanceOf(NegocioException.class);
        verify(projetoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Datas inválidas: previsão antes do início")
    void datasInvalidas() {
        var req = new ProjetoRequest(
                "P",
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                null,
                new BigDecimal("10000"),
                "d",
                1L,
                List.of(2L)
        );

        assertThatThrownBy(() -> projetoService.criar(req)).isInstanceOf(NegocioException.class);
    }

    @Test
    @DisplayName("Atualização de status inválida")
    void statusInvalido() {
        Projeto p = Projeto.builder()
                .id(5L)
                .status(StatusProjeto.EM_ANALISE)
                .gerente(gerente)
                .membrosAlocados(new java.util.HashSet<>(List.of(func1)))
                .build();
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> projetoService.atualizarStatus(5L, StatusProjeto.INICIADO))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    @DisplayName("Atualização de status válida")
    void statusValido() {
        Projeto p = Projeto.builder()
                .id(5L)
                .status(StatusProjeto.EM_ANALISE)
                .nome("X")
                .dataInicio(LocalDate.now())
                .previsaoTermino(LocalDate.now().plusMonths(1))
                .orcamentoTotal(BigDecimal.TEN)
                .gerente(gerente)
                .membrosAlocados(new java.util.HashSet<>(List.of(func1)))
                .build();
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));
        when(projetoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(projetoMapper.toResponse(any())).thenReturn(
                new ProjetoResponse(5L, "X", p.getDataInicio(), p.getPrevisaoTermino(), null, BigDecimal.TEN, "",
                        1L, "G", StatusProjeto.ANALISE_REALIZADA, br.com.portfolio.domain.enums.ClassificacaoRisco.BAIXO, List.of()));

        ProjetoResponse resp = projetoService.atualizarStatus(5L, StatusProjeto.ANALISE_REALIZADA);
        assertThat(resp.status()).isEqualTo(StatusProjeto.ANALISE_REALIZADA);

        ArgumentCaptor<Projeto> cap = ArgumentCaptor.forClass(Projeto.class);
        verify(projetoRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo(StatusProjeto.ANALISE_REALIZADA);
    }

    @Test
    @DisplayName("Atualização de projeto exclui id atual na contagem de limite")
    void atualizarRespeitaExclusaoDoIdNaContagem() {
        when(membroExternoService.obterEntidade(1L)).thenReturn(gerente);
        when(membroExternoService.obterEntidade(2L)).thenReturn(func1);
        when(projetoRepository.countProjetosAtivosPorMembro(eq(2L), eq(7L))).thenReturn(0L);

        Projeto existente = Projeto.builder()
                .id(7L)
                .nome("Old")
                .dataInicio(LocalDate.now())
                .previsaoTermino(LocalDate.now().plusMonths(2))
                .orcamentoTotal(new BigDecimal("5000"))
                .gerente(gerente)
                .status(StatusProjeto.EM_ANALISE)
                .membrosAlocados(new java.util.HashSet<>(List.of(func1)))
                .build();
        when(projetoRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(projetoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(projetoMapper.toResponse(any())).thenAnswer(inv -> {
            Projeto pr = inv.getArgument(0);
            return new ProjetoResponse(pr.getId(), pr.getNome(), pr.getDataInicio(), pr.getPrevisaoTermino(),
                    pr.getDataRealTermino(), pr.getOrcamentoTotal(), pr.getDescricao(),
                    pr.getGerente().getId(), pr.getGerente().getNome(), pr.getStatus(),
                    br.com.portfolio.domain.enums.ClassificacaoRisco.BAIXO, List.of());
        });

        var upd = new ProjetoUpdateRequest(
                "Novo",
                LocalDate.now(),
                LocalDate.now().plusMonths(3),
                null,
                new BigDecimal("8000"),
                "desc",
                1L,
                List.of(2L)
        );

        projetoService.atualizar(7L, upd);
        verify(projetoRepository).countProjetosAtivosPorMembro(2L, 7L);
    }
}
