package br.com.portfolio.repository;

import br.com.portfolio.domain.entity.Membro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembroRepository extends JpaRepository<Membro, Long> {
}
