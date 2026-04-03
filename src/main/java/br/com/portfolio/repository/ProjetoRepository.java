package br.com.portfolio.repository;

import br.com.portfolio.domain.entity.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProjetoRepository extends JpaRepository<Projeto, UUID>, JpaSpecificationExecutor<Projeto> {

    @Query(value = """
            SELECT COUNT(DISTINCT p.id) FROM projeto p
            INNER JOIN projeto_membro pm ON pm.projeto_id = p.id
            WHERE pm.membro_id = :membroId
            AND p.status NOT IN ('ENCERRADO', 'CANCELADO')
            AND (:projetoId IS NULL OR p.id <> :projetoId)
            """, nativeQuery = true)
    long countProjetosAtivosPorMembro(@Param("membroId") UUID membroId, @Param("projetoId") UUID projetoId);

    @Query("""
            SELECT p.status, COUNT(p), COALESCE(SUM(p.orcamentoTotal), 0)
            FROM Projeto p GROUP BY p.status
            """)
    java.util.List<Object[]> agregarPorStatus();

    @Query(value = """
            SELECT AVG((data_real_termino - data_inicio))
            FROM projeto
            WHERE status = 'ENCERRADO' AND data_real_termino IS NOT NULL
            """, nativeQuery = true)
    Double mediaDuracaoDiasProjetosEncerrados();

    @Query(value = "SELECT COUNT(DISTINCT membro_id) FROM projeto_membro", nativeQuery = true)
    long countMembrosUnicosAlocados();
}
