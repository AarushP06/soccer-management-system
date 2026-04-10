-- Add stadium metadata columns (city, country, capacity)
ALTER TABLE stadiums ADD COLUMN city varchar(255);
ALTER TABLE stadiums ADD COLUMN country varchar(255);
ALTER TABLE stadiums ADD COLUMN capacity integer;

