package br.com.portfolio.service;

import br.com.portfolio.domain.enums.StatusProjeto;
import br.com.portfolio.exception.NegocioException;
import org.springframework.stereotype.Component;

@Component
public class StatusProjetoValidator {

    public void validarTransicao(StatusProjeto atual, StatusProjeto novo) {
        if (novo == null) {
            throw new NegocioException("Status de destino é obrigatório.");
        }
        if (novo == StatusProjeto.CANCELADO) {
            if (atual == StatusProjeto.ENCERRADO || atual == StatusProjeto.CANCELADO) {
                throw new NegocioException("Não é possível aplicar cancelamento neste estado.");
            }
            return;
        }
        if (atual == StatusProjeto.CANCELADO || atual == StatusProjeto.ENCERRADO) {
            throw new NegocioException("Não é permitida alteração de status após encerramento ou cancelamento.");
        }
        if (novo.getOrdem() != atual.getOrdem() + 1) {
            throw new NegocioException("A transição de status deve respeitar a sequência, sem pular etapas.");
        }
    }
}
