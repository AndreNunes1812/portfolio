package br.com.portfolio.dto;

import br.com.portfolio.domain.enums.AtribuicaoMembro;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Cadastro de membro via API externa (mock)")
public record MembroExternoRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotNull AtribuicaoMembro atribuicao
) {
}
