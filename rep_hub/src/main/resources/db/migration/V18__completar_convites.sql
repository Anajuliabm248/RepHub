ALTER TABLE convite
    ADD COLUMN emissor_participacao_id BIGINT,
    ADD COLUMN utilizado_por_usuario_id BIGINT,
    ADD COLUMN participacao_criada_id BIGINT,
    ADD COLUMN data_utilizacao TIMESTAMP(6) WITHOUT TIME ZONE;

ALTER TABLE convite
    ADD CONSTRAINT fk_convite_emissor
        FOREIGN KEY (emissor_participacao_id) REFERENCES participacao (id),
    ADD CONSTRAINT fk_convite_usuario_utilizacao
        FOREIGN KEY (utilizado_por_usuario_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_convite_participacao_criada
        FOREIGN KEY (participacao_criada_id) REFERENCES participacao (id),
    ADD CONSTRAINT uc_convite_participacao_criada UNIQUE (participacao_criada_id);

-- NOT VALID preserva eventuais registros antigos incompletos, mas exige que todo
-- novo convite tenha os vínculos, as datas e o estado coerentes.
ALTER TABLE convite
    ADD CONSTRAINT ck_convite_dados_obrigatorios
        CHECK (republica_id IS NOT NULL
            AND emissor_participacao_id IS NOT NULL
            AND data_criacao IS NOT NULL
            AND data_expiracao IS NOT NULL) NOT VALID,
    ADD CONSTRAINT ck_convite_validade_48_horas
        CHECK (data_expiracao = data_criacao + INTERVAL '48 hours') NOT VALID,
    ADD CONSTRAINT ck_convite_estado
        CHECK (
            (status = 'DISPONIVEL'
                AND data_utilizacao IS NULL
                AND utilizado_por_usuario_id IS NULL
                AND participacao_criada_id IS NULL
                AND data_cancelamento IS NULL
                AND cancelado_por_participacao_id IS NULL)
            OR
            (status = 'UTILIZADO'
                AND data_utilizacao IS NOT NULL
                AND utilizado_por_usuario_id IS NOT NULL
                AND participacao_criada_id IS NOT NULL
                AND data_cancelamento IS NULL
                AND cancelado_por_participacao_id IS NULL)
            OR
            (status = 'CANCELADO'
                AND data_cancelamento IS NOT NULL
                AND cancelado_por_participacao_id IS NOT NULL
                AND data_utilizacao IS NULL
                AND utilizado_por_usuario_id IS NULL
                AND participacao_criada_id IS NULL)
        ) NOT VALID;

CREATE INDEX ix_convite_republica ON convite (republica_id);
CREATE UNIQUE INDEX uk_convite_codigo_normalizado ON convite (upper(codigo));
