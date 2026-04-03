package br.com.portfolio.dto;

import br.com.portfolio.domain.enums.ClassificacaoRisco;
import br.com.portfolio.domain.enums.StatusProjeto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Projeto com classificação de risco calculada")
public record ProjetoResponse(
        UUID id,
        String nome,
        LocalDate dataInicio,
        LocalDate previsaoTermino,
        LocalDate dataRealTermino,
        BigDecimal orcamentoTotal,
        String descricao,
        UUID gerenteId,
        String nomeGerente,
        StatusProjeto status,
        ClassificacaoRisco classificacaoRisco,
        List<MembroExternoResponse> membrosAlocados
) {
}
