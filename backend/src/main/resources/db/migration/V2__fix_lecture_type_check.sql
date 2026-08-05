-- V2: Fix lectures_lecture_type_check constraint
-- Dynamically finds and drops ANY check constraint on lectures.lecture_type,
-- then re-creates lectures_lecture_type_check with ('THEORY', 'LAB', 'EVENT').

DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = ANY(con.conkey)
        WHERE rel.relname = 'lectures'
          AND att.attname = 'lecture_type'
          AND con.contype = 'c'
    ) LOOP
        EXECUTE 'ALTER TABLE lectures DROP CONSTRAINT IF EXISTS ' || quote_ident(r.conname);
    END LOOP;
END $$;

ALTER TABLE lectures
    ADD CONSTRAINT lectures_lecture_type_check
        CHECK (lecture_type IN ('THEORY', 'LAB', 'EVENT'));
