-- Add external id columns for imported external references
ALTER TABLE leagues ADD COLUMN external_code varchar(255);
ALTER TABLE teams ADD COLUMN external_id varchar(255);
ALTER TABLE stadiums ADD COLUMN external_venue_id integer;
ALTER TABLE matches ADD COLUMN external_match_id varchar(255);

