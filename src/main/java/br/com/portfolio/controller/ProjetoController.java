package br.com.portfolio.controller;

import br.com.portfolio.domain.enums.StatusProjeto;
import br.com.portfolio.dto.ProjetoRequest;
import br.com.portfolio.dto.ProjetoResponse;
import br.com.portfolio.dto.ProjetoUpdateRequest;
import br.com.portfolio.dto.StatusProjetoUpdateRequest;
import br.com.portfolio.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projetos")
@RequiredArgsConstructor
@Tag(name = "Projetos", description = "CRUD de projetos, listagem com filtros e alteração de status")
@SecurityRequirement(name = "basicAuth")
public class ProjetoController {

    private final ProjetoService projetoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar projeto", description = "Status inicial: em análise. Exige 1–10 membros com atribuição funcionário e gerente com atribuição gerente.")
    public ProjetoResponse criar(@Valid @RequestBody ProjetoRequest request) {
        return projetoService.criar(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar projeto")
    public ProjetoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProjetoUpdateRequest request) {
        return projetoService.atualizar(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por id", description = "Inclui classificação de risco calculada.")
    public ProjetoResponse buscar(@PathVariable Long id) {
        return projetoService.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Listar projetos", description = "Paginação e filtros opcionais por nome (contém) e status.")
    public Page<ProjetoResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) StatusProjeto status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return projetoService.listar(nome, status, pageable);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar status", description = "Respeita a sequência de status ou permite cancelamento (exceto a partir de encerrado/cancelado).")
    public ProjetoResponse atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusProjetoUpdateRequest request) {
        return projetoService.atualizarStatus(id, request.novoStatus());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir projeto", description = "Bloqueado para status iniciado, em andamento ou encerrado.")
    public void excluir(@PathVariable Long id) {
        projetoService.excluir(id);
    }
}
