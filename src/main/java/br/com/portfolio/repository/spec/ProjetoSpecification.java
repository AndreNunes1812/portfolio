package br.com.portfolio.repository.spec;

import br.com.portfolio.domain.entity.Projeto;
import br.com.portfolio.domain.enums.StatusProjeto;
import org.springframework.data.jpa.domain.Specification;

public final class ProjetoSpecification {

    private ProjetoSpecification() {
    }

    public static Specification<Projeto> nomeContem(String trecho) {
        return (root, query, cb) -> {
            if (trecho == null || trecho.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("nome")), "%" + trecho.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Projeto> statusIgual(StatusProjeto status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Projeto> comFiltros(String nome, StatusProjeto status) {
        return Specification.where(nomeContem(nome)).and(statusIgual(status));
    }
}
