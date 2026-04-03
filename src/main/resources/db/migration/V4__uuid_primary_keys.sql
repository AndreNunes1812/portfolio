-- Recria tabelas com chaves UUID (remove dados anteriores em BIGINT).
DROP INDEX IF EXISTS idx_membro_nome_lower;
DROP TABLE IF EXISTS projeto_membro;
DROP TABLE IF EXISTS projeto;
DROP TABLE IF EXISTS membro;

CREATE TABLE membro (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome       VARCHAR(255) NOT NULL,
    atribuicao VARCHAR(40) NOT NULL
);

CREATE UNIQUE INDEX idx_membro_nome_lower ON membro (LOWER(nome));

CREATE TABLE projeto (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome                VARCHAR(255) NOT NULL,
    data_inicio         DATE NOT NULL,
    previsao_termino    DATE NOT NULL,
    data_real_termino   DATE,
    orcamento_total     NUMERIC(19, 2) NOT NULL,
    descricao           TEXT,
    gerente_id          UUID NOT NULL REFERENCES membro (id),
    status              VARCHAR(40) NOT NULL
);

CREATE TABLE projeto_membro (
    projeto_id UUID NOT NULL REFERENCES projeto (id) ON DELETE CASCADE,
    membro_id  UUID NOT NULL REFERENCES membro (id),
    PRIMARY KEY (projeto_id, membro_id)
);

CREATE INDEX idx_projeto_status ON projeto (status);
CREATE INDEX idx_projeto_membro_membro ON projeto_membro (membro_id);
