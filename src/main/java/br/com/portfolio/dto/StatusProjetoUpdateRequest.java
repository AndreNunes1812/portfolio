package br.com.portfolio.dto;

import br.com.portfolio.domain.enums.StatusProjeto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nova situação do projeto (respeita sequência ou cancelamento)")
public record StatusProjetoUpdateRequest(
        @NotNull StatusProjeto novoStatus
) {
}
