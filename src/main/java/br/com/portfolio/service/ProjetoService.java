package br.com.portfolio.service;

import br.com.portfolio.domain.entity.Membro;
import br.com.portfolio.domain.entity.Projeto;
import br.com.portfolio.domain.enums.AtribuicaoMembro;
import br.com.portfolio.domain.enums.StatusProjeto;
import br.com.portfolio.dto.ProjetoRequest;
import br.com.portfolio.dto.ProjetoResponse;
import br.com.portfolio.dto.ProjetoUpdateRequest;
import br.com.portfolio.exception.NegocioException;
import br.com.portfolio.exception.RecursoNaoEncontradoException;
import br.com.portfolio.repository.ProjetoRepository;
import br.com.portfolio.repository.spec.ProjetoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private static final int MIN_MEMBROS = 1;
    private static final int MAX_MEMBROS = 10;
    private static final int MAX_PROJETOS_ATIVOS_POR_MEMBRO = 3;

    private final ProjetoRepository projetoRepository;
    private final MembroExternoService membroExternoService;
    private final ProjetoMapper projetoMapper;
    private final StatusProjetoValidator statusProjetoValidator;

    @Transactional
    public ProjetoResponse criar(ProjetoRequest request) {
        validarDatas(request.dataInicio(), request.previsaoTermino());
        validarListaMembros(request.idsMembrosFuncionarios());

        Membro gerente = membroExternoService.obterEntidade(request.gerenteId());
        membroExternoService.garantirAtribuicao(gerente, AtribuicaoMembro.GERENTE);

        Set<Membro> funcionarios = carregarFuncionarios(request.idsMembrosFuncionarios());
        for (Membro m : funcionarios) {
            validarLimiteProjetosAtivosPorMembro(m.getId(), null);
        }

        Projeto projeto = Projeto.builder()
                .nome(request.nome().trim())
                .dataInicio(request.dataInicio())
                .previsaoTermino(request.previsaoTermino())
                .dataRealTermino(request.dataRealTermino())
                .orcamentoTotal(request.orcamentoTotal())
                .descricao(request.descricao())
                .gerente(gerente)
                .status(StatusProjeto.EM_ANALISE)
                .build();
        projeto.setMembrosAlocados(new HashSet<>(funcionarios));

        projeto = projetoRepository.save(projeto);
        projeto.getMembrosAlocados().size();
        return projetoMapper.toResponse(projeto);
    }

    @Transactional
    public ProjetoResponse atualizar(UUID id, ProjetoUpdateRequest request) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado: " + id));

        validarDatas(request.dataInicio(), request.previsaoTermino());
        validarListaMembros(request.idsMembrosFuncionarios());

        Membro gerente = membroExternoService.obterEntidade(request.gerenteId());
        membroExternoService.garantirAtribuicao(gerente, AtribuicaoMembro.GERENTE);

        Set<Membro> funcionarios = carregarFuncionarios(request.idsMembrosFuncionarios());
        for (Membro m : funcionarios) {
            validarLimiteProjetosAtivosPorMembro(m.getId(), id);
        }

        projeto.setNome(request.nome().trim());
        projeto.setDataInicio(request.dataInicio());
        projeto.setPrevisaoTermino(request.previsaoTermino());
        projeto.setDataRealTermino(request.dataRealTermino());
        projeto.setOrcamentoTotal(request.orcamentoTotal());
        projeto.setDescricao(request.descricao());
        projeto.setGerente(gerente);
        projeto.setMembrosAlocados(new HashSet<>(funcionarios));

        projeto = projetoRepository.save(projeto);
        projeto.getMembrosAlocados().size();
        return projetoMapper.toResponse(projeto);
    }

    @Transactional(readOnly = true)
    public ProjetoResponse buscarPorId(UUID id) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado: " + id));
        projeto.getGerente().getNome();
        projeto.getMembrosAlocados().size();
        return projetoMapper.toResponse(projeto);
    }

    @Transactional(readOnly = true)
    public Page<ProjetoResponse> listar(String nome, StatusProjeto status, Pageable pageable) {
        return projetoRepository
                .findAll(ProjetoSpecification.comFiltros(nome, status), pageable)
                .map(p -> {
                    p.getGerente().getNome();
                    p.getMembrosAlocados().size();
                    return projetoMapper.toResponse(p);
                });
    }

    @Transactional
    public ProjetoResponse atualizarStatus(UUID id, StatusProjeto novoStatus) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado: " + id));
        statusProjetoValidator.validarTransicao(projeto.getStatus(), novoStatus);
        projeto.setStatus(novoStatus);
        projeto = projetoRepository.save(projeto);
        projeto.getMembrosAlocados().size();
        return projetoMapper.toResponse(projeto);
    }

    @Transactional
    public void excluir(UUID id) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado: " + id));
        if (StatusProjeto.STATUS_PROIBE_EXCLUSAO.contains(projeto.getStatus())) {
            throw new NegocioException(
                    "Exclusão não permitida para projetos com status iniciado, em andamento ou encerrado.");
        }
        projetoRepository.delete(projeto);
    }

    private void validarDatas(LocalDate inicio, LocalDate previsao) {
        if (previsao.isBefore(inicio)) {
            throw new NegocioException("A previsão de término não pode ser anterior à data de início.");
        }
    }

    private void validarListaMembros(List<UUID> ids) {
        if (ids == null || ids.size() < MIN_MEMBROS || ids.size() > MAX_MEMBROS) {
            throw new NegocioException("Cada projeto deve ter entre " + MIN_MEMBROS + " e " + MAX_MEMBROS + " membros alocados.");
        }
        long distintos = ids.stream().distinct().count();
        if (distintos != ids.size()) {
            throw new NegocioException("Não é permitido repetir o mesmo membro na alocação.");
        }
    }

    private Set<Membro> carregarFuncionarios(List<UUID> ids) {
        return ids.stream()
                .map(membroExternoService::obterEntidade)
                .peek(m -> membroExternoService.garantirAtribuicao(m, AtribuicaoMembro.FUNCIONARIO))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private void validarLimiteProjetosAtivosPorMembro(UUID membroId, UUID projetoIdExcluido) {
        long ativos = projetoRepository.countProjetosAtivosPorMembro(membroId, projetoIdExcluido);
        if (ativos >= MAX_PROJETOS_ATIVOS_POR_MEMBRO) {
            throw new NegocioException(
                    "Membro " + membroId + " já está em " + MAX_PROJETOS_ATIVOS_POR_MEMBRO
                            + " projetos ativos (diferentes de encerrado ou cancelado).");
        }
    }
}
