ALTER TABLE convite
    ADD cancelado_por_participacao_id BIGINT;

ALTER TABLE convite
    ADD republica_id BIGINT;

ALTER TABLE convite
    ADD CONSTRAINT uc_convite_uuid UNIQUE (uuid);

ALTER TABLE convite
    ADD CONSTRAINT FK_CONVITE_ON_CANCELADO_POR_PARTICIPACAO FOREIGN KEY (cancelado_por_participacao_id) REFERENCES participacao (id);

ALTER TABLE convite
    ADD CONSTRAINT FK_CONVITE_ON_REPUBLICA FOREIGN KEY (republica_id) REFERENCES republica (id);