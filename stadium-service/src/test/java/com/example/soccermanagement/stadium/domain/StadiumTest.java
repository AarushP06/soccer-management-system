package com.example.soccermanagement.stadium.domain;

import com.example.soccermanagement.stadium.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests expected behavior and edge cases in the stadium service.
 */
class StadiumTest {

    @Test
    void createBuildsStadiumWithGeneratedId() {
        Stadium stadium = Stadium.create("Old Trafford");

        assertThat(stadium.getId()).isNotNull();
        assertThat(stadium.getName()).isEqualTo("Old Trafford");
        assertThat(stadium.getExternalVenueId()).isNull();
    }

    @Test
    void createFromExternalKeepsMetadata() {
        Stadium stadium = Stadium.createFromExternal("Anfield", 2, "Liverpool", "England", 54074);

        assertThat(stadium.getId()).isNotNull();
        assertThat(stadium.getName()).isEqualTo("Anfield");
        assertThat(stadium.getExternalVenueId()).isEqualTo(2);
        assertThat(stadium.getCity()).isEqualTo("Liverpool");
        assertThat(stadium.getCountry()).isEqualTo("England");
        assertThat(stadium.getCapacity()).isEqualTo(54074);
    }

    @Test
    void rehydrateRestoresState() {
        UUID id = UUID.randomUUID();

        Stadium stadium = Stadium.rehydrate(id, "Camp Nou", 3, "Barcelona", "Spain", 99354);

        assertThat(stadium.getId()).isEqualTo(id);
        assertThat(stadium.getName()).isEqualTo("Camp Nou");
        assertThat(stadium.getExternalVenueId()).isEqualTo(3);
        assertThat(stadium.getCity()).isEqualTo("Barcelona");
        assertThat(stadium.getCountry()).isEqualTo("Spain");
        assertThat(stadium.getCapacity()).isEqualTo(99354);
    }

    @Test
    void renameUpdatesName() {
        Stadium stadium = Stadium.create("Emirates Stadium");

        stadium.rename("Emirates Stadium Updated");

        assertThat(stadium.getName()).isEqualTo("Emirates Stadium Updated");
    }

    @Test
    void createRejectsBlankName() {
        assertThatThrownBy(() -> Stadium.create(" "))
                .isInstanceOf(DomainException.class)
                .hasMessage("Stadium name cannot be blank");
    }

    @Test
    void renameRejectsBlankName() {
        Stadium stadium = Stadium.create("San Siro");

        assertThatThrownBy(() -> stadium.rename(""))
                .isInstanceOf(DomainException.class)
                .hasMessage("Stadium name cannot be blank");
    }
}
