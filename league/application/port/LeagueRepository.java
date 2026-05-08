package league.application.port;

// ...existing code...
/**
 * Defines an abstraction used by the application layer in the application service.
 */
public interface LeagueRepository {
    // ...existing code...
    boolean existsByName(String name);
}

