-- Adds capacity column to stadiums to match the Stadium JPA entity
-- This migration is intentionally small and additive to avoid data loss.

ALTER TABLE stadiums
    ADD COLUMN IF NOT EXISTS capacity integer;

