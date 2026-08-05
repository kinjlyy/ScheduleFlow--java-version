-- V2: Fix lectures_lecture_type_check constraint
-- Root cause: when the Timetable Service first ran, LectureType only had THEORY and LAB.
-- EVENT was added later to LectureType.java but the DB constraint was never updated.
-- This migration drops the stale constraint and recreates it with all 3 current enum values:
--   THEORY, LAB, EVENT  (derived from com.scheduleflow.model.LectureType)

ALTER TABLE lectures
    DROP CONSTRAINT IF EXISTS lectures_lecture_type_check;

ALTER TABLE lectures
    ADD CONSTRAINT lectures_lecture_type_check
        CHECK (lecture_type IN ('THEORY', 'LAB', 'EVENT'));

-- Verification: the following query can be run manually to confirm the new constraint:
-- SELECT conname, pg_get_constraintdef(oid)
-- FROM pg_constraint
-- WHERE conname = 'lectures_lecture_type_check';
