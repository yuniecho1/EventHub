package au.edu.rmit.sept.webapp.RSVP;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;

/**
 * @brief Integration test class for {@link RSVPRepository}.
 *        Verifies CRUD operations and custom query methods.
 * 
 * Ensures:
 * - RSVPs are correctly saved and retrieved.
 * - Custom finder methods work as expected.
 * - Unique constraints are properly enforced.
 */
@SpringBootTest
public class RSVPRepositoryTest {

    @Autowired
    private RSVPRepository rsvpRepository;

    @BeforeEach
    public void setUp() {
        rsvpRepository.deleteAll(); // Clean slate for each test
    }

    /**
     * @brief Tests saving a valid RSVP and retrieving it by ID.
     *        Verifies all fields are correctly persisted.
     */
    @Test
    void shouldSaveAndFindRSVP_whenValidRSVP() {
        RSVP newRSVP = RSVPHelperTest.createDefaultRSVP("student@rmit.edu.au", 1L);
        RSVP savedRSVP = rsvpRepository.save(newRSVP);

        Optional<RSVP> found = rsvpRepository.findById(savedRSVP.getRsvpId());

        assertThat(found).isPresent();
        assertThat(found.get().getUserEmail()).isEqualTo("student@rmit.edu.au");
        assertThat(found.get().getEventId()).isEqualTo(1L);
        assertThat(found.get().getRsvpTimestamp()).isNotNull();
    }

    /**
     * @brief Tests finding all RSVPs for a specific event.
     *        Ensures that only RSVPs with the matching eventId are returned.
     */
    @Test
    void shouldReturnAllRSVPsForEvent_whenMultipleRSVPsExist() {
        Long eventId = 100L;
        List<RSVP> rsvpsForEvent = RSVPHelperTest.createMultipleRSVPsForEvent(eventId, "user@test.com", 3);
        rsvpsForEvent.forEach(rsvpRepository::save);

        List<RSVP> rsvpsForOtherEvent = RSVPHelperTest.createMultipleRSVPsForEvent(200L, "other@test.com", 2);
        rsvpsForOtherEvent.forEach(rsvpRepository::save);

        List<RSVP> foundRSVPs = rsvpRepository.findAllByEventId(eventId);

        assertThat(foundRSVPs).hasSize(3);
        assertThat(foundRSVPs).allMatch(rsvp -> rsvp.getEventId().equals(eventId));
    }

    /**
     * @brief Tests finding an RSVP by user email and event ID.
     *        Confirms the correct RSVP is returned when one exists.
     */
    @Test
    void shouldFindRSVPByUserEmailAndEventId_whenRSVPExists() {
        String userEmail = "student@rmit.edu.au";
        Long eventId = 1L;

        RSVP targetRSVP = RSVPHelperTest.createDefaultRSVP(userEmail, eventId);
        rsvpRepository.save(targetRSVP);

        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP("other@rmit.edu.au", eventId));
        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP(userEmail, 2L));

        RSVP foundRSVP = rsvpRepository.findByUserEmailAndEventId(userEmail, eventId);

        assertThat(foundRSVP).isNotNull();
        assertThat(foundRSVP.getUserEmail()).isEqualTo(userEmail);
        assertThat(foundRSVP.getEventId()).isEqualTo(eventId);
    }

    /**
     * @brief Tests that findByUserEmailAndEventId returns null when no RSVP exists.
     */
    @Test
    void shouldReturnNull_whenRSVPNotFoundByUserEmailAndEventId() {
        RSVP foundRSVP = rsvpRepository.findByUserEmailAndEventId("nonexistent@test.com", 999L);
        assertThat(foundRSVP).isNull();
    }

    /**
     * @brief Tests that saving a duplicate RSVP (same user and event) throws an exception.
     *        Verifies enforcement of unique constraints.
     */
    @Test
    void shouldThrowException_whenDuplicateRSVPSaved() {
        String userEmail = "student@rmit.edu.au";
        Long eventId = 1L;

        RSVP firstRSVP = RSVPHelperTest.createDefaultRSVP(userEmail, eventId);
        rsvpRepository.save(firstRSVP);

        RSVP duplicateRSVP = RSVPHelperTest.createDefaultRSVP(userEmail, eventId);

        assertThrows(Exception.class, () -> {
            rsvpRepository.save(duplicateRSVP);
            rsvpRepository.flush(); 
        });
    }

    /**
     * @brief Tests deleting an existing RSVP and confirms it no longer exists.
     */
    @Test
    void shouldDeleteRSVP_whenRSVPExists() {
        RSVP rsvp = RSVPHelperTest.createDefaultRSVP("student@rmit.edu.au", 1L);
        RSVP savedRSVP = rsvpRepository.save(rsvp);
        Long rsvpId = savedRSVP.getRsvpId();

        rsvpRepository.delete(savedRSVP);

        Optional<RSVP> found = rsvpRepository.findById(rsvpId);
        assertThat(found).isEmpty();
    }

    /**
     * @brief Tests counting and retrieving RSVPs for a specific event.
     *        Ensures correct count and matching event IDs.
     */
    @Test
    void shouldCountRSVPsForEvent_whenMultipleRSVPsExist() {
        Long eventId = 1L;
        List<RSVP> rsvps = RSVPHelperTest.createMultipleRSVPsForEvent(eventId, "user@test.com", 5);
        rsvps.forEach(rsvpRepository::save);

        long count = rsvpRepository.count();
        List<RSVP> foundRSVPs = rsvpRepository.findAllByEventId(eventId);

        assertThat(count).isEqualTo(5);
        assertThat(foundRSVPs).hasSize(5);
    }

    /**
     * @brief Tests that an empty list is returned when no RSVPs exist for an event.
     */
    @Test
    void shouldReturnEmptyList_whenNoRSVPsForEvent() {
        List<RSVP> foundRSVPs = rsvpRepository.findAllByEventId(999L);
        assertThat(foundRSVPs).isEmpty();
    }

    /**
     * @brief Tests finding all RSVPs for a specific user email.
     *        Ensures only RSVPs belonging to that user are returned.
     */
    @Test
    void shouldReturnAllRSVPsForUserEmail_whenRSVPsExist() {
        String targetUserEmail = "student@rmit.edu.au";
        String otherUserEmail = "other@rmit.edu.au";

        RSVP rsvp1 = RSVPHelperTest.createDefaultRSVP(targetUserEmail, 1L);
        RSVP rsvp2 = RSVPHelperTest.createDefaultRSVP(targetUserEmail, 2L);
        RSVP rsvp3 = RSVPHelperTest.createDefaultRSVP(targetUserEmail, 3L);

        rsvpRepository.save(rsvp1);
        rsvpRepository.save(rsvp2);
        rsvpRepository.save(rsvp3);

        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP(otherUserEmail, 1L));
        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP(otherUserEmail, 4L));

        List<RSVP> foundRSVPs = rsvpRepository.findAllByUserEmail(targetUserEmail);

        assertThat(foundRSVPs).hasSize(3);
        assertThat(foundRSVPs).allMatch(rsvp -> rsvp.getUserEmail().equals(targetUserEmail));

        List<Long> eventIds = foundRSVPs.stream()
            .map(RSVP::getEventId)
            .collect(java.util.stream.Collectors.toList());
        assertThat(eventIds).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    /**
     * @brief Tests that an empty list is returned when a user has no RSVPs.
     */
    @Test
    void shouldReturnEmptyList_whenUserHasNoRSVPs() {
        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP("other@rmit.edu.au", 1L));
        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP("another@rmit.edu.au", 2L));

        List<RSVP> foundRSVPs = rsvpRepository.findAllByUserEmail("nonexistent@rmit.edu.au");

        assertThat(foundRSVPs).isEmpty();
    }

    /**
     * @brief Tests retrieving RSVPs for multiple users with mixed data.
     *        Confirms results are correctly filtered by user email.
     */
    @Test
    void shouldReturnAllRSVPsForUser_whenMixedDataExists() {
        String user1 = "user1@test.com";
        String user2 = "user2@test.com";
        String user3 = "user3@test.com";

        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP(user1, 1L));
        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP(user1, 3L));
        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP(user1, 5L));

        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP(user2, 2L));
        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP(user2, 4L));

        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP(user3, 1L));
        rsvpRepository.save(RSVPHelperTest.createDefaultRSVP(user3, 2L));

        List<RSVP> user1RSVPs = rsvpRepository.findAllByUserEmail(user1);
        assertThat(user1RSVPs).hasSize(3);
        assertThat(user1RSVPs).allMatch(rsvp -> rsvp.getUserEmail().equals(user1));

        List<Long> user1EventIds = user1RSVPs.stream()
            .map(RSVP::getEventId)
            .collect(java.util.stream.Collectors.toList());
        assertThat(user1EventIds).containsExactlyInAnyOrder(1L, 3L, 5L);

        List<RSVP> user2RSVPs = rsvpRepository.findAllByUserEmail(user2);
        assertThat(user2RSVPs).hasSize(2);
        assertThat(user2RSVPs).allMatch(rsvp -> rsvp.getUserEmail().equals(user2));

        List<Long> user2EventIds = user2RSVPs.stream()
            .map(RSVP::getEventId)
            .collect(java.util.stream.Collectors.toList());
        assertThat(user2EventIds).containsExactlyInAnyOrder(2L, 4L);

        List<RSVP> user3RSVPs = rsvpRepository.findAllByUserEmail(user3);
        assertThat(user3RSVPs).hasSize(2);
        assertThat(user3RSVPs).allMatch(rsvp -> rsvp.getUserEmail().equals(user3));

        List<Long> user3EventIds = user3RSVPs.stream()
            .map(RSVP::getEventId)
            .collect(java.util.stream.Collectors.toList());
        assertThat(user3EventIds).containsExactlyInAnyOrder(1L, 2L);
    }
}