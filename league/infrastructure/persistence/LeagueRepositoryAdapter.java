package league.infrastructure.persistence;

// ...existing code...
/**
 * Adapts the domain repository contract to the persistence implementation.
 */
public class LeagueRepositoryAdapter implements LeagueRepository {
    // ...existing code...

    @Override
    public boolean existsByName(String name) {
        return springDataLeagueRepository.existsByName(name);
    }

    // ...existing code...
}

