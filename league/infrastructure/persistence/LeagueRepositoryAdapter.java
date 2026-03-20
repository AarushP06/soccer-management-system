package league.infrastructure.persistence;

// ...existing code...
public class LeagueRepositoryAdapter implements LeagueRepository {
    // ...existing code...

    @Override
    public boolean existsByName(String name) {
        return springDataLeagueRepository.existsByName(name);
    }

    // ...existing code...
}

