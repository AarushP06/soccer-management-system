insert into leagues (id, name) values
('11111111-1111-1111-1111-111111111111', 'Premier League');

insert into teams (id, name) values
('22222222-2222-2222-2222-222222222221', 'Arsenal'),
('22222222-2222-2222-2222-222222222222', 'Chelsea');

insert into stadiums (id, name) values
('33333333-3333-3333-3333-333333333333', 'Emirates Stadium');

insert into matches (id, league_id, home_team_id, away_team_id, stadium_id, status) values
('44444444-4444-4444-4444-444444444444',
 '11111111-1111-1111-1111-111111111111',
 '22222222-2222-2222-2222-222222222221',
 '22222222-2222-2222-2222-222222222222',
 '33333333-3333-3333-3333-333333333333',
 'SCHEDULED');
