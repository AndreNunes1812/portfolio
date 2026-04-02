package br.com.portfolio.service;

import br.com.portfolio.domain.enums.StatusProjeto;
import br.com.portfolio.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusProjetoValidatorTest {

    private StatusProjetoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StatusProjetoValidator();
    }

    @Test
    void sequenciaValida() {
        assertThatCode(() -> validator.validarTransicao(StatusProjeto.EM_ANALISE, StatusProjeto.ANALISE_REALIZADA))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validarTransicao(StatusProjeto.ANALISE_REALIZADA, StatusProjeto.ANALISE_APROVADA))
                .doesNotThrowAnyException();
    }

    @Test
    void naoPulaEtapas() {
        assertThatThrownBy(() -> validator.validarTransicao(StatusProjeto.EM_ANALISE, StatusProjeto.INICIADO))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void canceladoDeQualquerEstadoExcetoFinalizados() {
        assertThatCode(() -> validator.validarTransicao(StatusProjeto.PLANEJADO, StatusProjeto.CANCELADO))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> validator.validarTransicao(StatusProjeto.ENCERRADO, StatusProjeto.CANCELADO))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void naoAlteraAposCanceladoOuEncerrado() {
        assertThatThrownBy(() -> validator.validarTransicao(StatusProjeto.CANCELADO, StatusProjeto.EM_ANALISE))
                .isInstanceOf(NegocioException.class);
        assertThatThrownBy(() -> validator.validarTransicao(StatusProjeto.ENCERRADO, StatusProjeto.CANCELADO))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void novoStatusNulo() {
        assertThatThrownBy(() -> validator.validarTransicao(StatusProjeto.EM_ANALISE, null))
                .isInstanceOf(NegocioException.class);
    }
}
