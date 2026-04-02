package br.com.portfolio.service;

import br.com.portfolio.domain.enums.AtribuicaoMembro;
import br.com.portfolio.dto.MembroExternoRequest;
import br.com.portfolio.exception.NegocioException;
import br.com.portfolio.repository.MembroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
        when(membroRepository.save(any())).thenAnswer(inv -> {
            var m = inv.<br.com.portfolio.domain.entity.Membro>getArgument(0);
            m.setId(42L);
            return m;
        });

        var resp = membroExternoService.criar(new MembroExternoRequest(" Ana ", AtribuicaoMembro.FUNCIONARIO));

        assertThat(resp.id()).isEqualTo(42L);
        assertThat(resp.nome()).isEqualTo("Ana");
        ArgumentCaptor<br.com.portfolio.domain.entity.Membro> cap =
                ArgumentCaptor.forClass(br.com.portfolio.domain.entity.Membro.class);
        verify(membroRepository).save(cap.capture());
        assertThat(cap.getValue().getAtribuicao()).isEqualTo(AtribuicaoMembro.FUNCIONARIO);
    }

    @Test
    void garantirAtribuicaoFalha() {
        var m = br.com.portfolio.domain.entity.Membro.builder()
                .id(1L)
                .nome("x")
                .atribuicao(AtribuicaoMembro.FUNCIONARIO)
                .build();
        assertThatThrownBy(() -> membroExternoService.garantirAtribuicao(m, AtribuicaoMembro.GERENTE))
                .isInstanceOf(NegocioException.class);
    }
}
