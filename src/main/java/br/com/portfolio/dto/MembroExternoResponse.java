package br.com.portfolio.dto;

import br.com.portfolio.domain.enums.AtribuicaoMembro;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Membro retornado pela API externa")
public record MembroExternoResponse(
        Long id,
        String nome,
        AtribuicaoMembro atribuicao
) {
}
