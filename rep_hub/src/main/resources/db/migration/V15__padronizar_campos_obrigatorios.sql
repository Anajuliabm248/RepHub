
DO $$
DECLARE
    tabela TEXT;
BEGIN
    FOREACH tabela IN ARRAY ARRAY['usuarios', 'republica', 'convite', 'despesa', 'aviso', 'tarefa']
    LOOP
        EXECUTE format('UPDATE %I SET uuid = gen_random_uuid() WHERE uuid IS NULL', tabela);
        EXECUTE format('ALTER TABLE %I ALTER COLUMN uuid SET NOT NULL', tabela);
        EXECUTE format('CREATE UNIQUE INDEX %I ON %I (uuid)', 'uk_' || tabela || '_uuid', tabela);
    END LOOP;
END $$;

CREATE UNIQUE INDEX uk_usuarios_email_ci ON usuarios (lower(btrim(email))) WHERE email IS NOT NULL;

ALTER TABLE usuarios
    ADD CONSTRAINT ck_usuarios_obrigatorios
        CHECK (nome IS NOT NULL AND btrim(nome) <> '' AND email IS NOT NULL
               AND btrim(email) <> '' AND senha IS NOT NULL AND btrim(senha) <> '') NOT VALID;
ALTER TABLE republica
    ADD CONSTRAINT ck_republica_nome
        CHECK (nome IS NOT NULL AND btrim(nome) <> '') NOT VALID;
ALTER TABLE despesa
    ADD CONSTRAINT ck_despesa_titulo
        CHECK (titulo IS NOT NULL AND btrim(titulo) <> '') NOT VALID;
ALTER TABLE aviso
    ADD CONSTRAINT ck_aviso_textos
        CHECK (titulo IS NOT NULL AND btrim(titulo) <> ''
               AND mensagem IS NOT NULL AND btrim(mensagem) <> '') NOT VALID;
ALTER TABLE tarefa
    ADD CONSTRAINT ck_tarefa_titulo
        CHECK (titulo IS NOT NULL AND btrim(titulo) <> '') NOT VALID;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM despesa
               WHERE valor_total < 1 OR valor_total > 9999999999.99
                  OR valor_total <> round(valor_total, 2)) THEN
        RAISE EXCEPTION 'Há despesas com valor fora do intervalo ou com mais de duas casas decimais';
    END IF;
END $$;

ALTER TABLE despesa ALTER COLUMN valor_total TYPE NUMERIC(12, 2);
ALTER TABLE despesa ADD CONSTRAINT ck_despesa_valor_minimo CHECK (valor_total >= 1);
