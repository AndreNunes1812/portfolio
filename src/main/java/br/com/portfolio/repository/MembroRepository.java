package br.com.portfolio.repository;

import br.com.portfolio.domain.entity.Membro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MembroRepository extends JpaRepository<Membro, UUID> {

    boolean existsByNomeIgnoreCase(String nome);
}
