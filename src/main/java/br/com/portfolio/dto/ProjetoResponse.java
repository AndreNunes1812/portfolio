package br.com.portfolio.dto;

import br.com.portfolio.domain.enums.ClassificacaoRisco;
import br.com.portfolio.domain.enums.StatusProjeto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Projeto com classificação de risco calculada")
public record ProjetoResponse(
        Long id,
        String nome,
        LocalDate dataInicio,
        LocalDate previsaoTermino,
        LocalDate dataRealTermino,
        BigDecimal orcamentoTotal,
        String descricao,
        Long gerenteId,
        String nomeGerente,
        StatusProjeto status,
        ClassificacaoRisco classificacaoRisco,
        List<MembroExternoResponse> membrosAlocados
) {
}
