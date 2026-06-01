package au.edu.rmit.sept.webapp.Club;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.ClubRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.service.ClubService;

/**
 * @brief Integration test class for {@link ClubService}.
 *        Tests club creation, validation, and business logic.
 * 
 * @author Lucas Aponso
 * @version 1.0
 */
@SpringBootTest
@Transactional // Roll back after each test to avoid polluting the database
public class ClubServiceTest {

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User testUser;

    @BeforeEach
    public void setup() {
        eventRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUserEmail("test.organiser@example.com");
        testUser.setUserName("Test Organiser");
        testUser.setUserType("organiser");
        testUser.setUserPassword("password123");
        testUser = userRepository.save(testUser);
    }

    /**
     * @brief Tests creation of a valid club with all required fields.
     *
     * The test:
     * - Creates a valid club
     * - Saves it via ClubService
     * - Verifies fields are persisted correctly
     */
    @Test
    void createClub_shouldSucceed_whenValidFieldsProvided() {
        Club club = ClubHelperTest.createTechClub("Tech Innovators");
        Club savedClub = clubService.createClub(club, testUser);

        assertThat(savedClub).isNotNull();
        assertThat(savedClub.getClubId()).isNotNull();
        assertThat(savedClub.getClubName()).isEqualTo("Tech Innovators");
        assertThat(savedClub.getClubDescription()).isEqualTo("A club for technology enthusiasts and developers");
        assertThat(savedClub.getClubTag()).isEqualTo("tech");
        assertThat(savedClub.getClubCreationDate()).isNotNull();
    }

    /**
     * @brief Tests that club creation fails when name is null.
     *
     * The test:
     * - Creates a club with null name
     * - Attempts to save
     * - Expects IllegalArgumentException
     */
    @Test
    void createClub_shouldThrowException_whenNameIsNull() {
        Club club = ClubHelperTest.createClubWithNullField("name");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            clubService.createClub(club, testUser)
        );

        assertThat(exception.getMessage()).contains("Club Name cannot be null");
    }

    /**
     * @brief Tests that club creation fails when description is null.
     */
    @Test
    void createClub_shouldThrowException_whenDescriptionIsNull() {
        Club club = ClubHelperTest.createClubWithNullField("description");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            clubService.createClub(club, testUser)
        );

        assertThat(exception.getMessage()).contains("description cannot be null");
    }

    /**
     * @brief Tests that club creation fails when tag is null.
     */
    @Test
    void createClub_shouldThrowException_whenTagIsNull() {
        Club club = ClubHelperTest.createClubWithNullField("tag");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            clubService.createClub(club, testUser)
        );

        assertThat(exception.getMessage()).contains("Club tags cannot be null");
    }

    /**
     * @brief Tests creation of multiple clubs with unique attributes.
     *
     * The test:
     * - Creates 3 distinct clubs
     * - Verifies unique IDs and correct tags
     */
    @Test
    void createMultipleClubs_shouldAssignUniqueIds_andPreserveTags() {
        Club techClub = ClubHelperTest.createTechClub("Tech Club");
        Club sportsClub = ClubHelperTest.createSportsClub("Sports Club");
        Club musicClub = ClubHelperTest.createMusicClub("Music Club");

        Club savedTech = clubService.createClub(techClub, testUser);
        Club savedSports = clubService.createClub(sportsClub, testUser);
        Club savedMusic = clubService.createClub(musicClub, testUser);

        assertThat(savedTech.getClubId()).isNotEqualTo(savedSports.getClubId());
        assertThat(savedSports.getClubId()).isNotEqualTo(savedMusic.getClubId());
        assertThat(savedMusic.getClubId()).isNotEqualTo(savedTech.getClubId());
    }

    /**
     * @brief Tests validation method with valid club input.
     */
    @Test
    void validateClubNotNull_shouldPass_whenClubIsValid() {
        Club validClub = ClubHelperTest.createDefaultClub("Valid Club");
        clubService.validateClubNotNull(validClub);
    }

    /**
     * @brief Tests that club creation date is automatically set.
     */
    @Test
    void createClub_shouldAutoAssignCreationDate_whenSaved() {
        Club club = ClubHelperTest.createClub("Time Test Club", "Description", "test");
        Club savedClub = clubService.createClub(club, testUser);

        assertThat(savedClub.getClubCreationDate()).isNotNull();
    }

    /**
     * @brief Tests creating clubs with a range of different tags.
     */
    @Test
    void createClubs_shouldSucceed_whenTagsVary() {
        String[] tags = {"tech", "sport", "music", "art", "science"};

        for (int i = 0; i < tags.length; i++) {
            Club club = ClubHelperTest.createClub("Club " + (i + 1), "Description " + (i + 1), tags[i]);
            Club saved = clubService.createClub(club, testUser);
            assertThat(saved.getClubTag()).isEqualTo(tags[i]);
        }
    }

    /**
     * @brief Tests creating multiple clubs sharing the same tag.
     */
    @Test
    void createClubs_shouldAllowSameTag_whenNamesDiffer() {
        Club club1 = ClubHelperTest.createClub("Tech Club 1", "First tech club", "tech");
        Club club2 = ClubHelperTest.createClub("Tech Club 2", "Second tech club", "tech");

        Club saved1 = clubService.createClub(club1, testUser);
        Club saved2 = clubService.createClub(club2, testUser);

        assertThat(saved1.getClubTag()).isEqualTo("tech");
        assertThat(saved2.getClubTag()).isEqualTo("tech");
        assertThat(saved1.getClubId()).isNotEqualTo(saved2.getClubId());
    }

    /**
     * @brief Tests creating a club with special characters in its fields.
     */
    @Test
    void createClub_shouldHandleSpecialCharacters_whenFieldsContainSymbols() {
        Club club = ClubHelperTest.createClubWithSpecialCharacters();
        Club saved = clubService.createClub(club, testUser);

        assertThat(saved.getClubName()).contains("Special Characters");
        assertThat(saved.getClubTag()).isEqualTo("special-chars");
    }

    /**
     * @brief Tests that validation catches all null field combinations.
     */
    @Test
    void validateClubNotNull_shouldThrowException_whenAnyFieldIsNull() {
        Club nullName = ClubHelperTest.createClubWithNullField("name");
        Club nullDescription = ClubHelperTest.createClubWithNullField("description");
        Club nullTag = ClubHelperTest.createClubWithNullField("tag");

        assertThrows(IllegalArgumentException.class, () -> clubService.validateClubNotNull(nullName));
        assertThrows(IllegalArgumentException.class, () -> clubService.validateClubNotNull(nullDescription));
        assertThrows(IllegalArgumentException.class, () -> clubService.validateClubNotNull(nullTag));
    }

    /**
     * @brief Tests saving and retrieving a club from database.
     */
    @Test
    void createClub_shouldPersistInDatabase_whenValid() {
        Club club = ClubHelperTest.createSportsClub("Database Test Club");
        Club saved = clubService.createClub(club, testUser);

        boolean exists = clubRepository.existsById(saved.getClubId());
        assertThat(exists).isTrue();

        Club retrieved = clubRepository.findById(saved.getClubId()).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getClubName()).isEqualTo("Database Test Club");
    }

    /**
     * @brief Tests transactional rollback when validation fails.
     *
     * The test:
     * - Attempts invalid club creation
     * - Expects exception
     * - Verifies DB count unchanged
     */
    @Test
    void createClub_shouldNotPersist_whenValidationFails() {
        long initialCount = clubRepository.count();
        Club invalid = ClubHelperTest.createClubWithNullField("name");

        assertThrows(IllegalArgumentException.class, () ->
            clubService.createClub(invalid, testUser)
        );

        assertThat(clubRepository.count()).isEqualTo(initialCount);
    }

    /**
     * @brief Tests handling of clubs with empty string values.
     */
    @Test
    void createClub_shouldSucceed_whenFieldsAreEmptyStrings() {
        Club club = ClubHelperTest.createClubWithEmptyFields();
        Club saved = clubService.createClub(club, testUser);

        assertThat(saved.getClubId()).isNotNull();
        assertThat(saved.getClubName()).isEqualTo("");
        assertThat(saved.getClubDescription()).isEqualTo("");
        assertThat(saved.getClubTag()).isEqualTo("");
    }

    /**
     * @brief Tests that students are not permitted to create clubs.
     */
    @Test
    void createClub_shouldThrowException_whenUserIsStudent() {
        User student = new User();
        student.setUserEmail("student@example.com");
        student.setUserName("Test Student");
        student.setUserType("student");
        student.setUserPassword("password123");
        User savedStudent = userRepository.save(student);

        Club club = ClubHelperTest.createTechClub("Student Tech Club");

        Exception exception = assertThrows(IllegalStateException.class, () ->
            clubService.createClub(club, savedStudent)
        );

        assertThat(exception.getMessage()).contains("Students are not allowed to create clubs");
    }

    /**
     * @brief Tests that unauthenticated users cannot create clubs.
     */
    @Test
    void createClub_shouldThrowException_whenUserIsUnauthenticated() {
        Club club = ClubHelperTest.createTechClub("Unauthenticated Club");

        Exception exception = assertThrows(IllegalStateException.class, () ->
            clubService.createClub(club, null)
        );

        assertThat(exception.getMessage()).contains("User is not authenticated");
    }

    /**
     * @brief Tests that duplicate club names are not allowed.
     */
    @Test
    void createClub_shouldThrowException_whenDuplicateNameExists() {
        Club first = ClubHelperTest.createTechClub("Duplicate Club");
        clubService.createClub(first, testUser);

        Club duplicate = ClubHelperTest.createSportsClub("Duplicate Club");

        Exception exception = assertThrows(IllegalStateException.class, () ->
            clubService.createClub(duplicate, testUser)
        );

        assertThat(exception.getMessage()).contains("Club already exists with the same name");
    }
}