ALTER TABLE despesa
    ADD republica_id BIGINT;

ALTER TABLE despesa
    ALTER COLUMN republica_id SET NOT NULL;

ALTER TABLE despesa
    ADD CONSTRAINT FK_DESPESA_ON_REPUBLICA FOREIGN KEY (republica_id) REFERENCES republica (id);