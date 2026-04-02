package br.com.portfolio.service;

import br.com.portfolio.domain.entity.Membro;
import br.com.portfolio.domain.enums.AtribuicaoMembro;
import br.com.portfolio.dto.MembroExternoRequest;
import br.com.portfolio.dto.MembroExternoResponse;
import br.com.portfolio.exception.NegocioException;
import br.com.portfolio.exception.RecursoNaoEncontradoException;
import br.com.portfolio.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembroExternoService {

    private final MembroRepository membroRepository;

    @Transactional
    public MembroExternoResponse criar(MembroExternoRequest request) {
        Membro m = Membro.builder()
                .nome(request.nome().trim())
                .atribuicao(request.atribuicao())
                .build();
        m = membroRepository.save(m);
        return toResponse(m);
    }

    @Transactional(readOnly = true)
    public MembroExternoResponse buscarPorId(Long id) {
        Membro m = membroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não encontrado: " + id));
        return toResponse(m);
    }

    @Transactional(readOnly = true)
    public Page<MembroExternoResponse> listar(Pageable pageable) {
        return membroRepository.findAll(pageable).map(this::toResponse);
    }

    public Membro obterEntidade(Long id) {
        return membroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não encontrado: " + id));
    }

    public void garantirAtribuicao(Membro membro, AtribuicaoMembro esperada) {
        if (membro.getAtribuicao() != esperada) {
            throw new NegocioException(
                    "Operação exige membro com atribuição " + esperada + ".");
        }
    }

    private MembroExternoResponse toResponse(Membro m) {
        return new MembroExternoResponse(m.getId(), m.getNome(), m.getAtribuicao());
    }
}
