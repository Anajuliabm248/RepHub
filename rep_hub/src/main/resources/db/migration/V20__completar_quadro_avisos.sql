ALTER TABLE aviso
    ADD COLUMN autor_participacao_id BIGINT,
    ADD COLUMN ocultado_por_participacao_id BIGINT;

ALTER TABLE aviso
    ADD CONSTRAINT fk_aviso_autor
        FOREIGN KEY (autor_participacao_id) REFERENCES participacao (id),
    ADD CONSTRAINT fk_aviso_ocultado_por
        FOREIGN KEY (ocultado_por_participacao_id) REFERENCES participacao (id),
    ADD CONSTRAINT ck_aviso_autor_obrigatorio
        CHECK (autor_participacao_id IS NOT NULL) NOT VALID,
    ADD CONSTRAINT ck_aviso_ocultacao
        CHECK ((data_remocao IS NULL AND ocultado_por_participacao_id IS NULL)
            OR (data_remocao IS NOT NULL AND ocultado_por_participacao_id IS NOT NULL)) NOT VALID;

CREATE INDEX ix_aviso_republica_visivel
    ON aviso (republica_id, id DESC)
    WHERE data_remocao IS NULL;
