package br.com.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Criação de projeto (1 a 10 membros funcionários)")
public record ProjetoRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate previsaoTermino,
        LocalDate dataRealTermino,
        @NotNull @DecimalMin("0.0") BigDecimal orcamentoTotal,
        String descricao,
        @NotNull Long gerenteId,
        @NotEmpty @Size(min = 1, max = 10) List<Long> idsMembrosFuncionarios
) {
}
