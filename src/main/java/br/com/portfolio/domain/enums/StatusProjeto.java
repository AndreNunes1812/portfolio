package br.com.portfolio.domain.enums;

import java.util.EnumSet;
import java.util.Set;

public enum StatusProjeto {

    EM_ANALISE(0),
    ANALISE_REALIZADA(1),
    ANALISE_APROVADA(2),
    INICIADO(3),
    PLANEJADO(4),
    EM_ANDAMENTO(5),
    ENCERRADO(6),
    CANCELADO(7);

    private final int ordem;

    StatusProjeto(int ordem) {
        this.ordem = ordem;
    }

    public int getOrdem() {
        return ordem;
    }

    public static final Set<StatusProjeto> STATUS_PROIBE_EXCLUSAO = EnumSet.of(
            INICIADO,
            EM_ANDAMENTO,
            ENCERRADO
    );

    public static final Set<StatusProjeto> STATUS_PROJETO_FINALIZADO = EnumSet.of(ENCERRADO, CANCELADO);
}
