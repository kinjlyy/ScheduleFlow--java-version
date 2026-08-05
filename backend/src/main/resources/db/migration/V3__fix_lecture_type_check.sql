-- V3: Ensure lectures_lecture_type_check constraint includes THEORY, LAB, EVENT
ALTER TABLE lectures DROP CONSTRAINT IF EXISTS lectures_lecture_type_check;
ALTER TABLE lectures DROP CONSTRAINT IF EXISTS lectures_lecture_type_check1;
ALTER TABLE lectures DROP CONSTRAINT IF EXISTS lectures_lecture_type_check2;

ALTER TABLE lectures ADD CONSTRAINT lectures_lecture_type_check CHECK (lecture_type IN ('THEORY', 'LAB', 'EVENT'));
