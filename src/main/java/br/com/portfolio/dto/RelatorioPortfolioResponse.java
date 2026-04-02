package br.com.portfolio.dto;

import br.com.portfolio.domain.enums.StatusProjeto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Resumo do portfólio")
public record RelatorioPortfolioResponse(
        List<AgregadoPorStatus> porStatus,
        Double mediaDuracaoDiasProjetosEncerrados,
        Long totalMembrosUnicosAlocados
) {
    @Schema(description = "Quantidade e orçamento por status")
    public record AgregadoPorStatus(
            StatusProjeto status,
            long quantidadeProjetos,
            BigDecimal totalOrcado
    ) {
    }
}
