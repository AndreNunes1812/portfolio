package br.com.portfolio.controller;

import br.com.portfolio.dto.RelatorioPortfolioResponse;
import br.com.portfolio.service.RelatorioPortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Resumo do portfólio")
@SecurityRequirement(name = "basicAuth")
public class RelatorioPortfolioController {

    private final RelatorioPortfolioService relatorioPortfolioService;

    @GetMapping("/portfolio")
    @Operation(summary = "Relatório resumido do portfólio",
            description = "Quantidade e total orçado por status, média de duração (dias) dos encerrados e total de membros únicos alocados.")
    public RelatorioPortfolioResponse portfolio() {
        return relatorioPortfolioService.gerar();
    }
}
