package league.infrastructure.persistence;

// ...existing code...
/**
 * Declares Spring Data persistence operations for the application service.
 */
public interface SpringDataLeagueRepository extends JpaRepository<LeagueEntity, Long> {
    // ...existing code...
    boolean existsByName(String name);
}

