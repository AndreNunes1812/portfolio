package br.com.portfolio.service;

import br.com.portfolio.domain.enums.ClassificacaoRisco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClassificacaoRiscoServiceTest {

    private ClassificacaoRiscoService service;

    @BeforeEach
    void setUp() {
        service = new ClassificacaoRiscoService();
    }

    @Test
    @DisplayName("Baixo: orçamento até 100k e prazo até 3 meses")
    void baixoRisco() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        assertThat(service.calcular(new BigDecimal("100000"), inicio, inicio.plusMonths(3)))
                .isEqualTo(ClassificacaoRisco.BAIXO);
        assertThat(service.calcular(new BigDecimal("50000"), inicio, inicio.plusMonths(2)))
                .isEqualTo(ClassificacaoRisco.BAIXO);
    }

    @Test
    @DisplayName("Alto: orçamento acima de 500k")
    void altoPorOrcamento() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        assertThat(service.calcular(new BigDecimal("500001"), inicio, inicio.plusMonths(1)))
                .isEqualTo(ClassificacaoRisco.ALTO);
    }

    @Test
    @DisplayName("Alto: prazo superior a 6 meses")
    void altoPorPrazo() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        assertThat(service.calcular(new BigDecimal("1000"), inicio, inicio.plusMonths(7)))
                .isEqualTo(ClassificacaoRisco.ALTO);
    }

    @Test
    @DisplayName("Médio: orçamento entre 100.001 e 500.000")
    void medioPorOrcamento() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        assertThat(service.calcular(new BigDecimal("100001"), inicio, inicio.plusMonths(2)))
                .isEqualTo(ClassificacaoRisco.MEDIO);
        assertThat(service.calcular(new BigDecimal("250000"), inicio, inicio.plusMonths(1)))
                .isEqualTo(ClassificacaoRisco.MEDIO);
    }

    @Test
    @DisplayName("Médio: prazo entre 3 e 6 meses (exclusive 3, inclusive 6)")
    void medioPorPrazo() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        assertThat(service.calcular(new BigDecimal("50000"), inicio, inicio.plusMonths(4)))
                .isEqualTo(ClassificacaoRisco.MEDIO);
        assertThat(service.calcular(new BigDecimal("50000"), inicio, inicio.plusMonths(6)))
                .isEqualTo(ClassificacaoRisco.MEDIO);
    }

    @Test
    @DisplayName("Orçamento 500k exato não é alto por valor (limite alto é > 500k)")
    void limite500k() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        assertThat(service.calcular(new BigDecimal("500000"), inicio, inicio.plusMonths(1)))
                .isEqualTo(ClassificacaoRisco.MEDIO);
    }

    @Test
    @DisplayName("Argumentos nulos geram IllegalArgumentException")
    void nulos() {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> service.calcular(null, hoje, hoje))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.calcular(BigDecimal.ONE, null, hoje))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.calcular(BigDecimal.ONE, hoje, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Prazo negativo (previsão antes do início) trata meses como zero")
    void prazoNegativoViraZeroMeses() {
        LocalDate inicio = LocalDate.of(2026, 6, 1);
        LocalDate previsaoAntes = LocalDate.of(2026, 1, 1);
        assertThat(service.calcular(new BigDecimal("50000"), inicio, previsaoAntes))
                .isEqualTo(ClassificacaoRisco.BAIXO);
    }
}
