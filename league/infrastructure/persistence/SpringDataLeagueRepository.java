package league.infrastructure.persistence;

// ...existing code...
public interface SpringDataLeagueRepository extends JpaRepository<LeagueEntity, Long> {
    // ...existing code...
    boolean existsByName(String name);
}

