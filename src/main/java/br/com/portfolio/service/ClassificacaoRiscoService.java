package br.com.portfolio.service;

import br.com.portfolio.domain.enums.ClassificacaoRisco;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ClassificacaoRiscoService {

    private static final BigDecimal LIMITE_BAIXO_ORCAMENTO = new BigDecimal("100000");
    private static final BigDecimal LIMITE_ALTO_ORCAMENTO = new BigDecimal("500000");

    public ClassificacaoRisco calcular(BigDecimal orcamentoTotal, LocalDate dataInicio, LocalDate previsaoTermino) {
        if (orcamentoTotal == null || dataInicio == null || previsaoTermino == null) {
            throw new IllegalArgumentException("Orçamento e datas são obrigatórios para classificação de risco.");
        }
        long meses = ChronoUnit.MONTHS.between(dataInicio, previsaoTermino);
        if (meses < 0) {
            meses = 0;
        }

        if (orcamentoTotal.compareTo(LIMITE_ALTO_ORCAMENTO) > 0 || meses > 6) {
            return ClassificacaoRisco.ALTO;
        }

        boolean medioOrcamento = orcamentoTotal.compareTo(LIMITE_BAIXO_ORCAMENTO) > 0
                && orcamentoTotal.compareTo(LIMITE_ALTO_ORCAMENTO) <= 0;
        boolean medioPrazo = meses > 3 && meses <= 6;

        if (medioOrcamento || medioPrazo) {
            return ClassificacaoRisco.MEDIO;
        }

        return ClassificacaoRisco.BAIXO;
    }
}
