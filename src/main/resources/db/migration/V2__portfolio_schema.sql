CREATE TABLE membro (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(255) NOT NULL,
    atribuicao      VARCHAR(40) NOT NULL
);

CREATE TABLE projeto (
    id                  BIGSERIAL PRIMARY KEY,
    nome                VARCHAR(255) NOT NULL,
    data_inicio         DATE NOT NULL,
    previsao_termino    DATE NOT NULL,
    data_real_termino   DATE,
    orcamento_total     NUMERIC(19, 2) NOT NULL,
    descricao           TEXT,
    gerente_id          BIGINT NOT NULL REFERENCES membro (id),
    status              VARCHAR(40) NOT NULL
);

CREATE TABLE projeto_membro (
    projeto_id BIGINT NOT NULL REFERENCES projeto (id) ON DELETE CASCADE,
    membro_id  BIGINT NOT NULL REFERENCES membro (id),
    PRIMARY KEY (projeto_id, membro_id)
);

CREATE INDEX idx_projeto_status ON projeto (status);
CREATE INDEX idx_projeto_membro_membro ON projeto_membro (membro_id);
