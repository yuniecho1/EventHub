package au.edu.rmit.sept.webapp.Club;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.repository.ClubRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;

/**
 * @brief Integration test class for {@link ClubRepository}.
 *        Verifies CRUD operations for Club entities.
 * 
 * This class ensures that:
 * - Clubs are correctly saved, retrieved, updated, and deleted.
 * - Basic repository operations work as expected.
 * - Database integrity is maintained through transactional rollbacks.
 * 
 * @author 
 * @version 1.1
 */
@SpringBootTest
@Transactional  // Roll back after each test to ensure isolation
public class ClubRepositoryTest {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private EventRepository eventRepository;

    /**
     * @brief Helper method to create a test club.
     */
    private Club createTestClub(String name, String description, String tag) {
        Club club = new Club();
        club.setClubName(name);
        club.setClubDescription(description);
        club.setClubTag(tag);
        club.setClubCreationDate(LocalDateTime.now());
        return club;
    }

    /**
     * @brief Resets the database before each test to ensure isolation.
     */
    @BeforeEach
    public void setup() {
        eventRepository.deleteAll();
        clubRepository.deleteAll();
    }

    /**
     * @brief Tests saving a {@link Club} and retrieving it by its generated ID.
     * 
     * The test:
     * - Creates a new club
     * - Saves it to the repository
     * - Retrieves it back using findById
     * - Asserts that the saved and retrieved club match
     */
    @Test
    void saveAndFindClub_shouldReturnClub_whenSavedSuccessfully() {
        Club club = createTestClub("Tech Club", "A club for technology enthusiasts", "tech");
        Club savedClub = clubRepository.save(club);

        Optional<Club> found = clubRepository.findById(savedClub.getClubId());

        assertThat(found).isPresent();
        assertThat(found.get().getClubName()).isEqualTo("Tech Club");
        assertThat(found.get().getClubDescription()).isEqualTo("A club for technology enthusiasts");
        assertThat(found.get().getClubTag()).isEqualTo("tech");
        assertThat(found.get().getClubId()).isNotNull();
    }

    /**
     * @brief Tests finding a club by a non-existent ID.
     * 
     * The test:
     * - Calls findById with a missing club ID
     * - Verifies that the returned Optional is empty
     */
    @Test
    void findById_shouldReturnEmpty_whenClubDoesNotExist() {
        Optional<Club> found = clubRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    /**
     * @brief Tests saving multiple clubs and retrieving them all.
     * 
     * The test:
     * - Creates and saves several clubs
     * - Retrieves all clubs using findAll
     * - Verifies that all saved clubs are present
     */
    @Test
    void saveAndFindAllClubs_shouldReturnAllClubs_whenMultipleSaved() {
        Club club1 = createTestClub("Sports Club", "For sports activities", "sport");
        Club club2 = createTestClub("Music Society", "For music lovers", "music");
        Club club3 = createTestClub("Science Club", "For science enthusiasts", "science");

        clubRepository.save(club1);
        clubRepository.save(club2);
        clubRepository.save(club3);

        List<Club> allClubs = clubRepository.findAll();

        assertThat(allClubs).hasSize(3);
        assertThat(allClubs).extracting(Club::getClubName)
            .containsExactlyInAnyOrder("Sports Club", "Music Society", "Science Club");
    }

    /**
     * @brief Tests deleting a club by its ID.
     * 
     * The test:
     * - Saves a club
     * - Deletes it using deleteById
     * - Verifies it no longer exists
     */
    @Test
    void deleteById_shouldRemoveClub_whenClubExists() {
        Club club = createTestClub("Test Club", "Test Description", "test");
        Club savedClub = clubRepository.save(club);
        Long clubId = savedClub.getClubId();

        clubRepository.deleteById(clubId);

        Optional<Club> found = clubRepository.findById(clubId);
        assertThat(found).isEmpty();
    }

    /**
     * @brief Tests updating a club’s information.
     * 
     * The test:
     * - Creates and saves a club
     * - Updates its details
     * - Saves the updated club
     * - Verifies the changes persist
     */
    @Test
    void updateClub_shouldPersistChanges_whenClubUpdated() {
        Club club = createTestClub("Original Club", "Original Description", "original");
        Club savedClub = clubRepository.save(club);

        savedClub.setClubName("Updated Club");
        savedClub.setClubDescription("Updated Description");
        savedClub.setClubTag("updated");

        Club updatedClub = clubRepository.save(savedClub);

        Optional<Club> found = clubRepository.findById(updatedClub.getClubId());
        assertThat(found).isPresent();
        assertThat(found.get().getClubName()).isEqualTo("Updated Club");
        assertThat(found.get().getClubDescription()).isEqualTo("Updated Description");
        assertThat(found.get().getClubTag()).isEqualTo("updated");
    }

    /**
     * @brief Tests existence checking by club ID.
     * 
     * The test:
     * - Saves a club
     * - Verifies existsById returns true for the saved club
     * - Verifies existsById returns false for a missing club
     */
    @Test
    void existsById_shouldReturnTrueOrFalse_whenCheckingClubExistence() {
        Club club = createTestClub("Existing Club", "Description", "test");
        Club savedClub = clubRepository.save(club);

        boolean exists = clubRepository.existsById(savedClub.getClubId());
        boolean notExists = clubRepository.existsById(999L);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    /**
     * @brief Tests counting the total number of clubs.
     * 
     * The test:
     * - Starts empty
     * - Adds clubs incrementally
     * - Verifies count increases as expected
     */
    @Test
    void count_shouldIncrease_whenNewClubsAreSaved() {
        assertThat(clubRepository.count()).isEqualTo(0);

        clubRepository.save(createTestClub("Club 1", "Description 1", "tag1"));
        assertThat(clubRepository.count()).isEqualTo(1);

        clubRepository.save(createTestClub("Club 2", "Description 2", "tag2"));
        assertThat(clubRepository.count()).isEqualTo(2);
    }

    /**
     * @brief Tests automatic setting of the club creation date.
     * 
     * The test:
     * - Creates a new club
     * - Saves it
     * - Ensures creation date is automatically populated
     */
    @Test
    void clubCreationDate_shouldBeAutoSet_whenClubIsSaved() {
        Club club = createTestClub("Time Test Club", "Description", "time");
        LocalDateTime beforeSave = LocalDateTime.now().minusSeconds(1);

        Club savedClub = clubRepository.save(club);
        LocalDateTime afterSave = LocalDateTime.now().plusSeconds(1);

        assertThat(savedClub.getClubCreationDate()).isNotNull();
        assertThat(savedClub.getClubCreationDate()).isAfter(beforeSave);
        assertThat(savedClub.getClubCreationDate()).isBefore(afterSave);
    }

    /**
     * @brief Tests saving clubs with different tags.
     * 
     * The test:
     * - Saves clubs with unique tags
     * - Verifies all tags are stored correctly
     */
    @Test
    void saveClubs_shouldAllowDifferentTags_whenMultipleTagsProvided() {
        Club techClub = createTestClub("Tech Club", "Technology focused", "tech");
        Club sportsClub = createTestClub("Sports Club", "Sports activities", "sport");
        Club musicClub = createTestClub("Music Club", "Music and arts", "music");

        clubRepository.save(techClub);
        clubRepository.save(sportsClub);
        clubRepository.save(musicClub);

        List<Club> allClubs = clubRepository.findAll();

        assertThat(allClubs).hasSize(3);
        assertThat(allClubs).extracting(Club::getClubTag)
            .containsExactlyInAnyOrder("tech", "sport", "music");
    }

    /**
     * @brief Tests that club IDs are auto-generated and unique.
     * 
     * The test:
     * - Creates unsaved clubs with null IDs
     * - Saves them to the repository
     * - Ensures IDs are generated and distinct
     */
    @Test
    void clubId_shouldBeAutoGeneratedAndUnique_whenClubsAreSaved() {
        Club club1 = createTestClub("Club 1", "Description 1", "tag1");
        Club club2 = createTestClub("Club 2", "Description 2", "tag2");

        assertThat(club1.getClubId()).isNull();
        assertThat(club2.getClubId()).isNull();

        Club savedClub1 = clubRepository.save(club1);
        Club savedClub2 = clubRepository.save(club2);

        assertThat(savedClub1.getClubId()).isNotNull();
        assertThat(savedClub2.getClubId()).isNotNull();
        assertThat(savedClub1.getClubId()).isNotEqualTo(savedClub2.getClubId());
    }

    /**
     * @brief Tests deleting all clubs from the repository.
     * 
     * The test:
     * - Saves multiple clubs
     * - Calls deleteAll
     * - Verifies the repository is empty afterwards
     */
    @Test
    void deleteAll_shouldRemoveAllClubs_whenRepositoryNotEmpty() {
        clubRepository.save(createTestClub("Club 1", "Description 1", "tag1"));
        clubRepository.save(createTestClub("Club 2", "Description 2", "tag2"));
        clubRepository.save(createTestClub("Club 3", "Description 3", "tag3"));

        assertThat(clubRepository.count()).isEqualTo(3);

        clubRepository.deleteAll();

        assertThat(clubRepository.count()).isEqualTo(0);
        assertThat(clubRepository.findAll()).isEmpty();
    }
}