package br.com.portfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Raiz")
public class RootController {

    @GetMapping("/")
    @Operation(summary = "Informações básicas da API")
    public Map<String, String> root() {
        return Map.of(
                "service", "portfolio-backend",
                "docs", "/swagger-ui.html",
                "auth", "HTTP Basic (admin / admin)"
        );
    }
}
