package br.com.portfolio.service;

import br.com.portfolio.domain.entity.Membro;
import br.com.portfolio.domain.entity.Projeto;
import br.com.portfolio.domain.enums.AtribuicaoMembro;
import br.com.portfolio.domain.enums.ClassificacaoRisco;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    private static final UUID ID_GERENTE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID_FUNC = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ID_PROJ_5 = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ID_PROJ_7 = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID ID_PROJ_10 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ID_PROJ_99 = UUID.fromString("99999999-9999-9999-9999-999999999999");

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
        gerente = Membro.builder().id(ID_GERENTE).nome("G").atribuicao(AtribuicaoMembro.GERENTE).build();
        func1 = Membro.builder().id(ID_FUNC).nome("F").atribuicao(AtribuicaoMembro.FUNCIONARIO).build();

        lenient().doAnswer(invocation -> {
            Membro m = invocation.getArgument(0);
            AtribuicaoMembro esp = invocation.getArgument(1);
            if (m.getAtribuicao() != esp) {
                throw new NegocioException("Operação exige membro com atribuição " + esp + ".");
            }
            return null;
        }).when(membroExternoService).garantirAtribuicao(any(Membro.class), any(AtribuicaoMembro.class));
    }

    @Test
    @DisplayName("Exclusão bloqueada para iniciado, em andamento e encerrado")
    void excluirBloqueado() {
        for (StatusProjeto s : List.of(StatusProjeto.INICIADO, StatusProjeto.EM_ANDAMENTO, StatusProjeto.ENCERRADO)) {
            Projeto p = Projeto.builder().id(ID_PROJ_10).status(s).build();
            when(projetoRepository.findById(ID_PROJ_10)).thenReturn(Optional.of(p));
            assertThatThrownBy(() -> projetoService.excluir(ID_PROJ_10)).isInstanceOf(NegocioException.class);
        }
    }

    @Test
    @DisplayName("Exclusão permitida em análise")
    void excluirPermitido() {
        Projeto p = Projeto.builder().id(ID_PROJ_10).status(StatusProjeto.EM_ANALISE).build();
        when(projetoRepository.findById(ID_PROJ_10)).thenReturn(Optional.of(p));
        projetoService.excluir(ID_PROJ_10);
        verify(projetoRepository).delete(p);
    }

    @Test
    @DisplayName("Projeto inexistente ao excluir")
    void excluirNaoEncontrado() {
        when(projetoRepository.findById(ID_PROJ_99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projetoService.excluir(ID_PROJ_99)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Criação valida limite de 3 projetos ativos por membro")
    void limiteTresProjetos() {
        when(membroExternoService.obterEntidade(ID_GERENTE)).thenReturn(gerente);
        when(membroExternoService.obterEntidade(ID_FUNC)).thenReturn(func1);
        when(projetoRepository.countProjetosAtivosPorMembro(ID_FUNC, null)).thenReturn(3L);

        var req = new ProjetoRequest(
                "P",
                LocalDate.now(),
                LocalDate.now().plusMonths(2),
                null,
                new BigDecimal("10000"),
                "d",
                ID_GERENTE,
                List.of(ID_FUNC)
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
                ID_GERENTE,
                List.of(ID_FUNC)
        );

        assertThatThrownBy(() -> projetoService.criar(req)).isInstanceOf(NegocioException.class);
    }

    @Test
    @DisplayName("Atualização de status inválida")
    void statusInvalido() {
        Projeto p = Projeto.builder()
                .id(ID_PROJ_5)
                .status(StatusProjeto.EM_ANALISE)
                .gerente(gerente)
                .membrosAlocados(new java.util.HashSet<>(List.of(func1)))
                .build();
        when(projetoRepository.findById(ID_PROJ_5)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> projetoService.atualizarStatus(ID_PROJ_5, StatusProjeto.INICIADO))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    @DisplayName("Atualização de status válida")
    void statusValido() {
        Projeto p = Projeto.builder()
                .id(ID_PROJ_5)
                .status(StatusProjeto.EM_ANALISE)
                .nome("X")
                .dataInicio(LocalDate.now())
                .previsaoTermino(LocalDate.now().plusMonths(1))
                .orcamentoTotal(BigDecimal.TEN)
                .gerente(gerente)
                .membrosAlocados(new java.util.HashSet<>(List.of(func1)))
                .build();
        when(projetoRepository.findById(ID_PROJ_5)).thenReturn(Optional.of(p));
        when(projetoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(projetoMapper.toResponse(any())).thenReturn(
                new ProjetoResponse(ID_PROJ_5, "X", p.getDataInicio(), p.getPrevisaoTermino(), null, BigDecimal.TEN, "",
                        ID_GERENTE, "G", StatusProjeto.ANALISE_REALIZADA, br.com.portfolio.domain.enums.ClassificacaoRisco.BAIXO, List.of()));

        ProjetoResponse resp = projetoService.atualizarStatus(ID_PROJ_5, StatusProjeto.ANALISE_REALIZADA);
        assertThat(resp.status()).isEqualTo(StatusProjeto.ANALISE_REALIZADA);

        ArgumentCaptor<Projeto> cap = ArgumentCaptor.forClass(Projeto.class);
        verify(projetoRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo(StatusProjeto.ANALISE_REALIZADA);
    }

    @Test
    @DisplayName("Atualização de projeto exclui id atual na contagem de limite")
    void atualizarRespeitaExclusaoDoIdNaContagem() {
        when(membroExternoService.obterEntidade(ID_GERENTE)).thenReturn(gerente);
        when(membroExternoService.obterEntidade(ID_FUNC)).thenReturn(func1);
        when(projetoRepository.countProjetosAtivosPorMembro(eq(ID_FUNC), eq(ID_PROJ_7))).thenReturn(0L);

        Projeto existente = Projeto.builder()
                .id(ID_PROJ_7)
                .nome("Old")
                .dataInicio(LocalDate.now())
                .previsaoTermino(LocalDate.now().plusMonths(2))
                .orcamentoTotal(new BigDecimal("5000"))
                .gerente(gerente)
                .status(StatusProjeto.EM_ANALISE)
                .membrosAlocados(new java.util.HashSet<>(List.of(func1)))
                .build();
        when(projetoRepository.findById(ID_PROJ_7)).thenReturn(Optional.of(existente));
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
                ID_GERENTE,
                List.of(ID_FUNC)
        );

        projetoService.atualizar(ID_PROJ_7, upd);
        verify(projetoRepository).countProjetosAtivosPorMembro(ID_FUNC, ID_PROJ_7);
    }

    @Test
    @DisplayName("Criação com sucesso persiste projeto em análise")
    void criarSucesso() {
        when(membroExternoService.obterEntidade(ID_GERENTE)).thenReturn(gerente);
        when(membroExternoService.obterEntidade(ID_FUNC)).thenReturn(func1);
        when(projetoRepository.countProjetosAtivosPorMembro(ID_FUNC, null)).thenReturn(0L);
        when(projetoRepository.save(any())).thenAnswer(inv -> {
            Projeto pr = inv.getArgument(0);
            pr.setId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
            return pr;
        });
        when(projetoMapper.toResponse(any())).thenAnswer(inv -> {
            Projeto pr = inv.getArgument(0);
            return new ProjetoResponse(pr.getId(), pr.getNome(), pr.getDataInicio(), pr.getPrevisaoTermino(),
                    pr.getDataRealTermino(), pr.getOrcamentoTotal(), pr.getDescricao(),
                    pr.getGerente().getId(), pr.getGerente().getNome(), pr.getStatus(),
                    ClassificacaoRisco.BAIXO, List.of());
        });

        var req = new ProjetoRequest(
                " Novo ",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 1),
                null,
                new BigDecimal("10000"),
                "desc",
                ID_GERENTE,
                List.of(ID_FUNC)
        );

        ProjetoResponse resp = projetoService.criar(req);
        assertThat(resp.status()).isEqualTo(StatusProjeto.EM_ANALISE);
        assertThat(resp.nome()).isEqualTo("Novo");

        ArgumentCaptor<Projeto> cap = ArgumentCaptor.forClass(Projeto.class);
        verify(projetoRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo(StatusProjeto.EM_ANALISE);
    }

    @Test
    @DisplayName("Busca por id retorna projeto quando existe")
    void buscarPorIdEncontrado() {
        Projeto p = Projeto.builder()
                .id(ID_PROJ_5)
                .nome("P")
                .dataInicio(LocalDate.now())
                .previsaoTermino(LocalDate.now().plusMonths(1))
                .orcamentoTotal(BigDecimal.TEN)
                .gerente(gerente)
                .status(StatusProjeto.EM_ANALISE)
                .membrosAlocados(new java.util.HashSet<>(List.of(func1)))
                .build();
        when(projetoRepository.findById(ID_PROJ_5)).thenReturn(Optional.of(p));
        when(projetoMapper.toResponse(p)).thenReturn(
                new ProjetoResponse(ID_PROJ_5, "P", p.getDataInicio(), p.getPrevisaoTermino(), null, BigDecimal.TEN, "",
                        ID_GERENTE, "G", StatusProjeto.EM_ANALISE, ClassificacaoRisco.BAIXO, List.of()));

        ProjetoResponse r = projetoService.buscarPorId(ID_PROJ_5);
        assertThat(r.nome()).isEqualTo("P");
    }

    @Test
    void buscarPorIdNaoEncontrado() {
        when(projetoRepository.findById(ID_PROJ_99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projetoService.buscarPorId(ID_PROJ_99))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void listarComPagina() {
        Projeto p = Projeto.builder()
                .id(ID_PROJ_5)
                .nome("L")
                .dataInicio(LocalDate.now())
                .previsaoTermino(LocalDate.now().plusMonths(1))
                .orcamentoTotal(BigDecimal.ONE)
                .gerente(gerente)
                .status(StatusProjeto.EM_ANALISE)
                .membrosAlocados(new java.util.HashSet<>(List.of(func1)))
                .build();
        Pageable pg = PageRequest.of(0, 20);
        when(projetoRepository.findAll(any(Specification.class), eq(pg)))
                .thenReturn(new PageImpl<>(List.of(p), pg, 1));
        when(projetoMapper.toResponse(any())).thenReturn(
                new ProjetoResponse(ID_PROJ_5, "L", p.getDataInicio(), p.getPrevisaoTermino(), null, BigDecimal.ONE, "",
                        ID_GERENTE, "G", StatusProjeto.EM_ANALISE, ClassificacaoRisco.BAIXO, List.of()));

        assertThat(projetoService.listar(null, null, pg).getContent()).hasSize(1);
    }

    @Test
    void atualizarProjetoNaoEncontrado() {
        when(projetoRepository.findById(ID_PROJ_99)).thenReturn(Optional.empty());
        var upd = new ProjetoUpdateRequest(
                "X",
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                null,
                BigDecimal.ONE,
                "",
                ID_GERENTE,
                List.of(ID_FUNC)
        );
        assertThatThrownBy(() -> projetoService.atualizar(ID_PROJ_99, upd))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizarStatusProjetoNaoEncontrado() {
        when(projetoRepository.findById(ID_PROJ_99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projetoService.atualizarStatus(ID_PROJ_99, StatusProjeto.ANALISE_REALIZADA))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void criarExigeGerenteComAtribuicaoGerente() {
        Membro naoGerente = Membro.builder().id(ID_GERENTE).nome("X").atribuicao(AtribuicaoMembro.FUNCIONARIO).build();
        when(membroExternoService.obterEntidade(ID_GERENTE)).thenReturn(naoGerente);

        var req = new ProjetoRequest(
                "P",
                LocalDate.now(),
                LocalDate.now().plusMonths(2),
                null,
                BigDecimal.TEN,
                "d",
                ID_GERENTE,
                List.of(ID_FUNC)
        );
        assertThatThrownBy(() -> projetoService.criar(req)).isInstanceOf(NegocioException.class);
    }

    @Test
    void criarExigeFuncionariosNaEquipe() {
        Membro gerenteErrado = Membro.builder().id(ID_FUNC).nome("G").atribuicao(AtribuicaoMembro.GERENTE).build();
        when(membroExternoService.obterEntidade(ID_GERENTE)).thenReturn(gerente);
        when(membroExternoService.obterEntidade(ID_FUNC)).thenReturn(gerenteErrado);

        var req = new ProjetoRequest(
                "P",
                LocalDate.now(),
                LocalDate.now().plusMonths(2),
                null,
                BigDecimal.TEN,
                "d",
                ID_GERENTE,
                List.of(ID_FUNC)
        );
        assertThatThrownBy(() -> projetoService.criar(req)).isInstanceOf(NegocioException.class);
    }

    @Test
    void criarRejeitaMembrosDuplicadosNaLista() {
        var req = new ProjetoRequest(
                "P",
                LocalDate.now(),
                LocalDate.now().plusMonths(2),
                null,
                BigDecimal.TEN,
                "d",
                ID_GERENTE,
                List.of(ID_FUNC, ID_FUNC)
        );
        assertThatThrownBy(() -> projetoService.criar(req)).isInstanceOf(NegocioException.class);
    }

    @Test
    void criarRejeitaListaNulaOuVaziaOuAcimaDoMaximo() {
        var base = new ProjetoRequest(
                "P",
                LocalDate.now(),
                LocalDate.now().plusMonths(2),
                null,
                BigDecimal.TEN,
                "d",
                ID_GERENTE,
                List.of(ID_FUNC)
        );
        assertThatThrownBy(() -> projetoService.criar(new ProjetoRequest(
                base.nome(), base.dataInicio(), base.previsaoTermino(), base.dataRealTermino(),
                base.orcamentoTotal(), base.descricao(), base.gerenteId(), null)))
                .isInstanceOf(NegocioException.class);

        assertThatThrownBy(() -> projetoService.criar(new ProjetoRequest(
                base.nome(), base.dataInicio(), base.previsaoTermino(), base.dataRealTermino(),
                base.orcamentoTotal(), base.descricao(), base.gerenteId(), List.of())))
                .isInstanceOf(NegocioException.class);

        List<UUID> onze = IntStream.range(0, 11).mapToObj(i -> UUID.randomUUID()).toList();
        assertThatThrownBy(() -> projetoService.criar(new ProjetoRequest(
                base.nome(), base.dataInicio(), base.previsaoTermino(), base.dataRealTermino(),
                base.orcamentoTotal(), base.descricao(), base.gerenteId(), onze)))
                .isInstanceOf(NegocioException.class);
    }
}
