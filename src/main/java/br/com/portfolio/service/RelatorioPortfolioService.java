package br.com.portfolio.service;

import br.com.portfolio.domain.enums.StatusProjeto;
import br.com.portfolio.dto.RelatorioPortfolioResponse;
import br.com.portfolio.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioPortfolioService {

    private final ProjetoRepository projetoRepository;

    @Transactional(readOnly = true)
    public RelatorioPortfolioResponse gerar() {
        List<RelatorioPortfolioResponse.AgregadoPorStatus> porStatus = new ArrayList<>();
        for (Object[] row : projetoRepository.agregarPorStatus()) {
            StatusProjeto status = (StatusProjeto) row[0];
            long qtd = toLong(row[1]);
            BigDecimal total = (BigDecimal) row[2];
            porStatus.add(new RelatorioPortfolioResponse.AgregadoPorStatus(status, qtd, total));
        }
        porStatus.sort(Comparator.comparing(a -> a.status().getOrdem()));

        Double mediaDias = projetoRepository.mediaDuracaoDiasProjetosEncerrados();
        long membrosUnicos = projetoRepository.countMembrosUnicosAlocados();

        return new RelatorioPortfolioResponse(porStatus, mediaDias, membrosUnicos);
    }

    private static long toLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        throw new IllegalStateException("Valor numérico inesperado: " + value);
    }
}
