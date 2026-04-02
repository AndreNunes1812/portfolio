package br.com.portfolio.service;

import br.com.portfolio.domain.entity.Membro;
import br.com.portfolio.domain.entity.Projeto;
import br.com.portfolio.domain.enums.AtribuicaoMembro;
import br.com.portfolio.domain.enums.ClassificacaoRisco;
import br.com.portfolio.domain.enums.StatusProjeto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProjetoMapperTest {

    @Test
    void mapeiaProjetoComRisco() {
        ClassificacaoRiscoService risco = new ClassificacaoRiscoService();
        ProjetoMapper mapper = new ProjetoMapper(risco);

        Membro g = Membro.builder().id(1L).nome("G").atribuicao(AtribuicaoMembro.GERENTE).build();
        Membro f = Membro.builder().id(2L).nome("F").atribuicao(AtribuicaoMembro.FUNCIONARIO).build();
        Set<Membro> set = new HashSet<>();
        set.add(f);

        LocalDate ini = LocalDate.of(2025, 1, 1);
        Projeto p = Projeto.builder()
                .id(9L)
                .nome("Proj")
                .dataInicio(ini)
                .previsaoTermino(ini.plusMonths(2))
                .orcamentoTotal(new BigDecimal("50000"))
                .descricao("d")
                .gerente(g)
                .status(StatusProjeto.EM_ANALISE)
                .build();
        p.setMembrosAlocados(set);

        var dto = mapper.toResponse(p);

        assertThat(dto.classificacaoRisco()).isEqualTo(ClassificacaoRisco.BAIXO);
        assertThat(dto.membrosAlocados()).hasSize(1);
        assertThat(dto.gerenteId()).isEqualTo(1L);
    }
}
