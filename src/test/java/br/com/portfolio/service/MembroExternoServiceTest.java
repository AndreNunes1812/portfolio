package br.com.portfolio.service;

import br.com.portfolio.domain.entity.Membro;
import br.com.portfolio.domain.enums.AtribuicaoMembro;
import br.com.portfolio.dto.MembroExternoRequest;
import br.com.portfolio.exception.NegocioException;
import br.com.portfolio.exception.RecursoNaoEncontradoException;
import br.com.portfolio.repository.MembroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembroExternoServiceTest {

    @Mock
    private MembroRepository membroRepository;

    @InjectMocks
    private MembroExternoService membroExternoService;

    @Test
    void criarPersisteMembro() {
        when(membroRepository.existsByNomeIgnoreCase(anyString())).thenReturn(false);
        var idFixo = UUID.fromString("00000000-0000-0000-0000-000000000042");
        when(membroRepository.save(any())).thenAnswer(inv -> {
            var m = inv.<br.com.portfolio.domain.entity.Membro>getArgument(0);
            m.setId(idFixo);
            return m;
        });

        var resp = membroExternoService.criar(new MembroExternoRequest(" Ana ", AtribuicaoMembro.FUNCIONARIO));

        assertThat(resp.id()).isEqualTo(idFixo);
        assertThat(resp.nome()).isEqualTo("Ana");
        ArgumentCaptor<br.com.portfolio.domain.entity.Membro> cap =
                ArgumentCaptor.forClass(br.com.portfolio.domain.entity.Membro.class);
        verify(membroRepository).save(cap.capture());
        assertThat(cap.getValue().getAtribuicao()).isEqualTo(AtribuicaoMembro.FUNCIONARIO);
    }

    @Test
    void criarRejeitaNomeDuplicado() {
        when(membroRepository.existsByNomeIgnoreCase("Andre Souza Nunes")).thenReturn(true);

        assertThatThrownBy(() -> membroExternoService.criar(
                        new MembroExternoRequest("Andre Souza Nunes", AtribuicaoMembro.FUNCIONARIO)))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("nome");

        verify(membroRepository, never()).save(any());
    }

    @Test
    void garantirAtribuicaoFalha() {
        var m = br.com.portfolio.domain.entity.Membro.builder()
                .id(UUID.fromString("10000000-0000-0000-0000-000000000001"))
                .nome("x")
                .atribuicao(AtribuicaoMembro.FUNCIONARIO)
                .build();
        assertThatThrownBy(() -> membroExternoService.garantirAtribuicao(m, AtribuicaoMembro.GERENTE))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void garantirAtribuicaoSucesso() {
        var m = Membro.builder()
                .id(UUID.fromString("10000000-0000-0000-0000-000000000002"))
                .nome("ok")
                .atribuicao(AtribuicaoMembro.FUNCIONARIO)
                .build();
        membroExternoService.garantirAtribuicao(m, AtribuicaoMembro.FUNCIONARIO);
    }

    @Test
    void buscarPorIdRetornaMembro() {
        UUID id = UUID.fromString("30000000-0000-0000-0000-000000000003");
        var m = Membro.builder().id(id).nome("N").atribuicao(AtribuicaoMembro.GERENTE).build();
        when(membroRepository.findById(id)).thenReturn(Optional.of(m));

        var resp = membroExternoService.buscarPorId(id);
        assertThat(resp.id()).isEqualTo(id);
        assertThat(resp.nome()).isEqualTo("N");
    }

    @Test
    void buscarPorIdNaoEncontrado() {
        UUID id = UUID.fromString("40000000-0000-0000-0000-000000000004");
        when(membroRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membroExternoService.buscarPorId(id))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void listarPagina() {
        UUID id = UUID.fromString("50000000-0000-0000-0000-000000000005");
        var m = Membro.builder().id(id).nome("L").atribuicao(AtribuicaoMembro.FUNCIONARIO).build();
        Pageable pg = PageRequest.of(0, 10);
        when(membroRepository.findAll(pg)).thenReturn(new PageImpl<>(List.of(m), pg, 1));

        var page = membroExternoService.listar(pg);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().nome()).isEqualTo("L");
    }

    @Test
    void obterEntidadeRetornaMembro() {
        UUID id = UUID.fromString("60000000-0000-0000-0000-000000000006");
        var m = Membro.builder().id(id).nome("E").atribuicao(AtribuicaoMembro.FUNCIONARIO).build();
        when(membroRepository.findById(id)).thenReturn(Optional.of(m));

        assertThat(membroExternoService.obterEntidade(id)).isSameAs(m);
    }

    @Test
    void obterEntidadeNaoEncontrado() {
        UUID id = UUID.fromString("61000000-0000-0000-0000-000000000006");
        when(membroRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membroExternoService.obterEntidade(id))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
