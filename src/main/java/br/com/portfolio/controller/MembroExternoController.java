package br.com.portfolio.controller;

import br.com.portfolio.dto.MembroExternoRequest;
import br.com.portfolio.dto.MembroExternoResponse;
import br.com.portfolio.service.MembroExternoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/external/members")
@RequiredArgsConstructor
@Tag(name = "Membros (API externa)", description = "Mock de API externa para cadastro e consulta de membros (nome e atribuição).")
@SecurityRequirement(name = "basicAuth")
public class MembroExternoController {

    private final MembroExternoService membroExternoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar membro")
    public MembroExternoResponse criar(@Valid @RequestBody MembroExternoRequest request) {
        return membroExternoService.criar(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar membro por id")
    public MembroExternoResponse buscar(@PathVariable UUID id) {
        return membroExternoService.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Listar membros")
    public Page<MembroExternoResponse> listar(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return membroExternoService.listar(pageable);
    }
}
