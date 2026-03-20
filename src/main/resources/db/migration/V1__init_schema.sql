create table leagues (
    id uuid primary key,
    name varchar(255) not null
);

create table teams (
    id uuid primary key,
    name varchar(255) not null
);

create table stadiums (
    id uuid primary key,
    name varchar(255) not null
);

create table matches (
    id uuid primary key,
    league_id uuid not null,
    home_team_id uuid not null,
    away_team_id uuid not null,
    stadium_id uuid not null,
    status varchar(50) not null
);
