ALTER TABLE tarefa
    ADD COLUMN criador_participacao_id BIGINT,
    ADD COLUMN autor_cancelamento_id BIGINT;

ALTER TABLE tarefa
    ADD CONSTRAINT fk_tarefa_criador
        FOREIGN KEY (criador_participacao_id) REFERENCES participacao (id),
    ADD CONSTRAINT fk_tarefa_autor_cancelamento
        FOREIGN KEY (autor_cancelamento_id) REFERENCES participacao (id),
    ADD CONSTRAINT ck_tarefa_criador_obrigatorio
        CHECK (criador_participacao_id IS NOT NULL) NOT VALID,
    ADD CONSTRAINT ck_tarefa_cancelamento
        CHECK ((data_cancelamento IS NULL AND autor_cancelamento_id IS NULL)
            OR (data_cancelamento IS NOT NULL AND autor_cancelamento_id IS NOT NULL)) NOT VALID;

ALTER TABLE atribuicao_tarefa
    ADD CONSTRAINT ck_atribuicao_estado
        CHECK (
            (status = 'PENDENTE'
                AND data_conclusao IS NULL
                AND data_cancelamento IS NULL
                AND autor_cancelamento_id IS NULL)
            OR
            (status = 'CONCLUIDA'
                AND data_conclusao IS NOT NULL
                AND data_cancelamento IS NULL
                AND autor_cancelamento_id IS NULL)
            OR
            (status = 'CANCELADA'
                AND data_conclusao IS NULL
                AND data_cancelamento IS NOT NULL
                AND autor_cancelamento_id IS NOT NULL)
        ) NOT VALID;

-- Garante a regra mesmo quando duas requisições tentam atribuir ao mesmo tempo.
CREATE UNIQUE INDEX uk_atribuicao_tarefa_responsavel_aberta
    ON atribuicao_tarefa (tarefa_id, participacao_responsavel_id)
    WHERE status <> 'CANCELADA';

CREATE INDEX ix_tarefa_republica ON tarefa (republica_id);
CREATE INDEX ix_atribuicao_tarefa ON atribuicao_tarefa (tarefa_id);
CREATE INDEX ix_atribuicao_responsavel_status
    ON atribuicao_tarefa (participacao_responsavel_id, status);
