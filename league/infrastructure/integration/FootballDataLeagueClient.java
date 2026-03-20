// ...existing code...
import league.domain.exception.LeagueNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
// ...existing code...

@Component
public class FootballDataLeagueClient {
    // ...existing code...

    public CompetitionDto getCompetitionByCode(String code) {
        try {
            return restTemplate.getForObject(baseUrl + "/competitions/{code}", CompetitionDto.class, code);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new LeagueNotFoundException("External competition not found for code: " + code);
            }
            throw ex;
        }
    }

    public record CompetitionDto(String name) {}

    // ...existing code...
}

