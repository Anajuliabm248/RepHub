ALTER TABLE participacao
    ALTER COLUMN papel TYPE VARCHAR(255) USING CASE papel
        WHEN 0 THEN 'MORADOR'
        WHEN 1 THEN 'ADMINISTRADOR'
        ELSE NULL
    END;

UPDATE participacao SET status = 'ATIVO' WHERE status = 'Ativo';

ALTER TABLE participacao
    ADD COLUMN usuario_id BIGINT,
    ADD COLUMN republica_id BIGINT,
    ADD COLUMN data_saida TIMESTAMP(6) WITHOUT TIME ZONE;

ALTER TABLE participacao
    ADD CONSTRAINT fk_participacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_participacao_republica FOREIGN KEY (republica_id) REFERENCES republica (id),
    ADD CONSTRAINT ck_participacao_obrigatorios
        CHECK (usuario_id IS NOT NULL AND republica_id IS NOT NULL
               AND papel IS NOT NULL AND status IS NOT NULL) NOT VALID;

UPDATE participacao SET uuid = gen_random_uuid() WHERE uuid IS NULL;
ALTER TABLE participacao ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE participacao ADD CONSTRAINT uc_participacao_uuid UNIQUE (uuid);

CREATE UNIQUE INDEX uk_participacao_usuario_ativo
    ON participacao (usuario_id) WHERE status = 'ATIVO';
