package br.com.portfolio.service;

import br.com.portfolio.domain.entity.Membro;
import br.com.portfolio.domain.entity.Projeto;
import br.com.portfolio.dto.MembroExternoResponse;
import br.com.portfolio.dto.ProjetoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class ProjetoMapper {

    private final ClassificacaoRiscoService classificacaoRiscoService;

    public ProjetoResponse toResponse(Projeto p) {
        var risco = classificacaoRiscoService.calcular(
                p.getOrcamentoTotal(),
                p.getDataInicio(),
                p.getPrevisaoTermino());

        var membros = p.getMembrosAlocados().stream()
                .sorted(Comparator.comparing(Membro::getId))
                .map(m -> new MembroExternoResponse(m.getId(), m.getNome(), m.getAtribuicao()))
                .toList();

        return new ProjetoResponse(
                p.getId(),
                p.getNome(),
                p.getDataInicio(),
                p.getPrevisaoTermino(),
                p.getDataRealTermino(),
                p.getOrcamentoTotal(),
                p.getDescricao(),
                p.getGerente().getId(),
                p.getGerente().getNome(),
                p.getStatus(),
                risco,
                membros
        );
    }
}
