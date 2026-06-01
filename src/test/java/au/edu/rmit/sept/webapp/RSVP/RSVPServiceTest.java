package au.edu.rmit.sept.webapp.RSVP;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import au.edu.rmit.sept.webapp.Event.EventHelperTest;
import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.repository.ClubRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;

/**
 * @brief Integration test class for {@link RSVPService} with real database events.
 *        Tests the getRSVPEventsForUser method by creating real Event entities
 *        in the database for comprehensive integration testing.
 * 
 * @author Agampreet Singh
 * @version 1.0
 */
@SpringBootTest
public class RSVPServiceTest {

    @Autowired
    private RSVPService rsvpService;

    @Autowired
    private RSVPRepository rsvpRepository;
    
    @Autowired
    private EventService eventService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private EventRepository eventRepository;

    private final String testUserEmail = "student@rmit.edu.au";
    private Long testEventId;

    // Store created event IDs for cleanup and reference
    private Long event1Id;
    private Long event2Id;
    private Long event3Id;
    private Long event4Id;

    /**
     * Creates a test Club object for use in events.
     * @param clubName the club name
     * @param clubDescription the club description
     * @param clubTag the club tag
     * @return a test {@link Club} object
     */
    private Club createTestClub(String clubName, String clubDescription, String clubTag) {
        Club club = new Club();
        club.setClubName(clubName);
        club.setClubDescription(clubDescription);
        club.setClubTag(clubTag);
        return club;
    }

    @BeforeEach
    public void setUp() {
        // Clean up before each test to ensure fresh state
        eventRepository.deleteAll();
        clubRepository.deleteAll();
        rsvpRepository.deleteAll();
        createTestEvents();
    }

    private void createTestEvents() {
        // Add timestamp to ensure unique event titles across test runs
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Create and save test clubs
        Club savedClub1 = clubRepository.save(createTestClub("Sports Club", "a sports club", "sport"));
        Club savedClub2 = clubRepository.save(createTestClub("Academic Club", "an academic club", "academic"));
        Club savedClub3 = clubRepository.save(createTestClub("Social Club", "a social club", "social"));
        Club savedClub4 = clubRepository.save(createTestClub("Entertainment Club", "an entertainment club", "entertainment"));

        Event event1 = EventHelperTest.createEvent("Test Event 1 " + timestamp, 
            LocalDate.now().plusDays(7), savedClub1, 
            "Description for test event 1", "Melbourne CBD", "50", "Sports");
        Event savedEvent1 = eventService.createEvent(event1);
        event1Id = savedEvent1.getEventId();

        Event event2 = EventHelperTest.createEvent("Test Event 2 " + timestamp, 
            LocalDate.now().plusDays(14), savedClub2, 
            "Description for test event 2", "RMIT Campus", "Free", "Academic");
        event2.setClub(savedClub2);
        Event savedEvent2 = eventService.createEvent(event2);
        event2Id = savedEvent2.getEventId();

        Event event3 = EventHelperTest.createEvent("Test Event 3 " + timestamp, 
            LocalDate.now().plusDays(21), savedClub3, 
            "Description for test event 3", "South Bank", "25", "Social");
        Event savedEvent3 = eventService.createEvent(event3);
        event3Id = savedEvent3.getEventId();

        Event event4 = EventHelperTest.createEvent("Test Event 4 " + timestamp, 
            LocalDate.now().plusDays(28), savedClub4, 
            "Description for test event 4", "St Kilda", "75", "Entertainment");
        Event savedEvent4 = eventService.createEvent(event4);
        event4Id = savedEvent4.getEventId();

        testEventId = event1Id;
    }

    /**
     * @brief Tests that hasUserRSVPd returns false when the user has not RSVP’d.
     */
    @Test
    void hasUserRSVPd_shouldReturnFalse_whenNoRSVPExists() {
        boolean hasRSVPd = rsvpService.hasUserRSVPd(testUserEmail, testEventId);
        assertThat(hasRSVPd).isFalse();
    }

    /**
     * @brief Tests that hasUserRSVPd returns true when an RSVP exists for the user and event.
     */
    @Test
    void hasUserRSVPd_shouldReturnTrue_whenRSVPExists() {
        RSVP rsvp = RSVPHelperTest.createDefaultRSVP(testUserEmail, testEventId);
        rsvpRepository.save(rsvp);

        boolean hasRSVPd = rsvpService.hasUserRSVPd(testUserEmail, testEventId);
        assertThat(hasRSVPd).isTrue();
    }

    /**
     * @brief Tests creating an RSVP for a user and verifies the data is correctly stored.
     */
    @Test
    void createRSVP_shouldPersistRSVP_whenCalled() {
        RSVP createdRSVP = rsvpService.createRSVP(testUserEmail, testEventId);

        assertThat(createdRSVP).isNotNull();
        assertThat(createdRSVP.getRsvpId()).isNotNull();
        assertThat(createdRSVP.getUserEmail()).isEqualTo(testUserEmail);
        assertThat(createdRSVP.getEventId()).isEqualTo(testEventId);
        assertThat(createdRSVP.getRsvpTimestamp()).isNotNull();

        boolean hasRSVPd = rsvpService.hasUserRSVPd(testUserEmail, testEventId);
        assertThat(hasRSVPd).isTrue();
    }

    /**
     * @brief Tests removing an existing RSVP and ensures it no longer exists.
     */
    @Test
    void removeRSVP_shouldDeleteRSVP_whenExists() {
        RSVP rsvp = RSVPHelperTest.createDefaultRSVP(testUserEmail, testEventId);
        rsvpRepository.save(rsvp);

        assertThat(rsvpService.hasUserRSVPd(testUserEmail, testEventId)).isTrue();
        rsvpService.removeRSVP(testUserEmail, testEventId);
        assertThat(rsvpService.hasUserRSVPd(testUserEmail, testEventId)).isFalse();
    }

    /**
     * @brief Tests that toggleRSVP creates a new RSVP when none exists.
     */
    @Test
    void toggleRSVP_shouldCreateRSVP_whenNoneExists() {
        assertThat(rsvpService.hasUserRSVPd(testUserEmail, testEventId)).isFalse();
        boolean result = rsvpService.toggleRSVP(testUserEmail, testEventId);
        assertThat(result).isTrue();
        assertThat(rsvpService.hasUserRSVPd(testUserEmail, testEventId)).isTrue();
    }

    /**
     * @brief Tests that multiple toggles correctly alternate RSVP creation and deletion.
     */
    @Test
    void toggleRSVP_shouldAlternateRSVPState_whenCalledMultipleTimes() {
        assertThat(rsvpService.hasUserRSVPd(testUserEmail, testEventId)).isFalse();

        // First toggle - should create RSVP
        boolean result1 = rsvpService.toggleRSVP(testUserEmail, testEventId);
        assertThat(result1).isTrue();
        assertThat(rsvpService.hasUserRSVPd(testUserEmail, testEventId)).isTrue();

        // Second toggle - should remove RSVP
        boolean result2 = rsvpService.toggleRSVP(testUserEmail, testEventId);
        assertThat(result2).isFalse();
        assertThat(rsvpService.hasUserRSVPd(testUserEmail, testEventId)).isFalse();

        // Third toggle - should create RSVP again
        boolean result3 = rsvpService.toggleRSVP(testUserEmail, testEventId);
        assertThat(result3).isTrue();
        assertThat(rsvpService.hasUserRSVPd(testUserEmail, testEventId)).isTrue();
    }

    /**
     * @brief Tests retrieving all RSVPs for a given event.
     */
    @Test
    void getRSVPsForEvent_shouldReturnRSVPs_whenRSVPsExistForEvent() {
        List<RSVP> event1RSVPs = RSVPHelperTest.createMultipleRSVPsForEvent(event1Id, "user@test.com", 3);
        event1RSVPs.forEach(rsvpRepository::save);

        // Create RSVPs for event2
        List<RSVP> event2RSVPs = RSVPHelperTest.createMultipleRSVPsForEvent(event2Id, "other@test.com", 2);
        event2RSVPs.forEach(rsvpRepository::save);

        // Get RSVPs for event1
        List<RSVP> result = rsvpService.getRSVPsForEvent(event1Id);
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(rsvp -> rsvp.getEventId().equals(event1Id));
    }

    /**
     * @brief Tests retrieving RSVPs for an event with none.
     */
    @Test
    void getRSVPsForEvent_shouldReturnEmptyList_whenNoRSVPsExist() {
        List<RSVP> result = rsvpService.getRSVPsForEvent(999L);
        assertThat(result).isEmpty();
    }

    /**
     * @brief Tests RSVP functionality for different users on the same event.
     */
    @Test
    void toggleRSVP_shouldHandleMultipleUsersIndependently_whenDifferentUsersRSVP() {
        String user1Email = "user1@test.com";
        String user2Email = "user2@test.com";

        // User 1 RSVPs
        boolean result1 = rsvpService.toggleRSVP(user1Email, event1Id);
        assertThat(result1).isTrue();
        assertThat(rsvpService.hasUserRSVPd(user1Email, event1Id)).isTrue();
        assertThat(rsvpService.hasUserRSVPd(user2Email, event1Id)).isFalse();

        // User 2 RSVPs
        boolean result2 = rsvpService.toggleRSVP(user2Email, event1Id);
        assertThat(result2).isTrue();
        assertThat(rsvpService.hasUserRSVPd(user1Email, event1Id)).isTrue();
        assertThat(rsvpService.hasUserRSVPd(user2Email, event1Id)).isTrue();

        // User 1 un-RSVPs
        boolean result3 = rsvpService.toggleRSVP(user1Email, event1Id);
        assertThat(result3).isFalse();
        assertThat(rsvpService.hasUserRSVPd(user1Email, event1Id)).isFalse();
        assertThat(rsvpService.hasUserRSVPd(user2Email, event1Id)).isTrue();
    }

    /**
     * @brief Tests RSVP functionality for the same user across different events.
     */
    @Test
    void toggleRSVP_shouldHandleMultipleEventsIndependently_whenSameUserRSVPsDifferentEvents() {
        String userEmail = "student@rmit.edu.au";

        // RSVP to event 1
        boolean result1 = rsvpService.toggleRSVP(userEmail, event1Id);
        assertThat(result1).isTrue();
        assertThat(rsvpService.hasUserRSVPd(userEmail, event1Id)).isTrue();
        assertThat(rsvpService.hasUserRSVPd(userEmail, event2Id)).isFalse();

        // RSVP to event 2
        boolean result2 = rsvpService.toggleRSVP(userEmail, event2Id);
        assertThat(result2).isTrue();
        assertThat(rsvpService.hasUserRSVPd(userEmail, event1Id)).isTrue();
        assertThat(rsvpService.hasUserRSVPd(userEmail, event2Id)).isTrue();

        // Un-RSVP from event 1
        boolean result3 = rsvpService.toggleRSVP(userEmail, event1Id);
        assertThat(result3).isFalse();
        assertThat(rsvpService.hasUserRSVPd(userEmail, event1Id)).isFalse();
        assertThat(rsvpService.hasUserRSVPd(userEmail, event2Id)).isTrue();
    }

    /**
     * @brief Tests retrieving RSVPs for a specific user.
     */
    @Test
    void getRSVPsForUser_shouldReturnOnlyUserRSVPs_whenMultipleUsersExist() {
        String targetUserEmail = "student@rmit.edu.au";
        String otherUserEmail = "other@rmit.edu.au";

        List<RSVP> targetUserRSVPs = RSVPHelperTest.createMultipleRSVPsForUser(targetUserEmail, 1L, 3);
        targetUserRSVPs.forEach(rsvpRepository::save);

        List<RSVP> otherUserRSVPs = RSVPHelperTest.createMultipleRSVPsForUser(otherUserEmail, 10L, 2);
        otherUserRSVPs.forEach(rsvpRepository::save);

        List<RSVP> result = rsvpService.getRSVPsForUser(targetUserEmail);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(rsvp -> rsvp.getUserEmail().equals(targetUserEmail));
        assertThat(result.stream().mapToLong(RSVP::getEventId))
            .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    /**
     * @brief Tests that retrieving RSVPs for a user with none returns an empty list.
     */
    @Test
    void getRSVPsForUser_shouldReturnEmptyList_whenNoRSVPsExist() {
        List<RSVP> result = rsvpService.getRSVPsForUser("noRSVPs@test.com");
        assertThat(result).isEmpty();
    }

    /**
     * @brief Tests that retrieving RSVPs for a user returns empty when only others have RSVPs.
     */
    @Test
    void getRSVPsForUser_shouldReturnEmptyList_whenOnlyOtherUsersHaveRSVPs() {
        String targetUserEmail = "target@test.com";
        String otherUserEmail = "other@test.com";

        List<RSVP> otherUserRSVPs = RSVPHelperTest.createMultipleRSVPsForUser(otherUserEmail, 1L, 5);
        otherUserRSVPs.forEach(rsvpRepository::save);

        List<RSVP> result = rsvpService.getRSVPsForUser(targetUserEmail);
        assertThat(result).isEmpty();
    }

    /**
     * @brief Tests retrieving RSVPs for multiple users across multiple events.
     */
    @Test
    void getRSVPsForUser_shouldReturnCorrectEvents_whenMultipleUsersExist() {
        String user1Email = "user1@test.com";
        String user2Email = "user2@test.com";
        Long event1Id = 1L;
        Long event2Id = 2L;
        Long event3Id = 3L;

        rsvpService.createRSVP(user1Email, event1Id);
        rsvpService.createRSVP(user1Email, event2Id);

        rsvpService.createRSVP(user2Email, event2Id);
        rsvpService.createRSVP(user2Email, event3Id);

        List<RSVP> user1RSVPs = rsvpService.getRSVPsForUser(user1Email);
        assertThat(user1RSVPs).hasSize(2);
        assertThat(user1RSVPs.stream().mapToLong(RSVP::getEventId))
            .containsExactlyInAnyOrder(event1Id, event2Id);

        List<RSVP> user2RSVPs = rsvpService.getRSVPsForUser(user2Email);
        assertThat(user2RSVPs).hasSize(2);
        assertThat(user2RSVPs.stream().mapToLong(RSVP::getEventId))
            .containsExactlyInAnyOrder(event2Id, event3Id);
    }

    /**
     * @brief Tests retrieving all events a user has RSVP’d to.
     */
    @Test
    void getRSVPEventsForUser_shouldReturnCorrectEvents_whenUserHasRSVPs() {
        String targetUserEmail = "student@rmit.edu.au";
        String otherUserEmail = "other@rmit.edu.au";
        
        // Target user RSVPs to events 1 and 3
        RSVP rsvp1 = RSVPHelperTest.createDefaultRSVP(targetUserEmail, event1Id);
        RSVP rsvp3 = RSVPHelperTest.createDefaultRSVP(targetUserEmail, event3Id);
        rsvpRepository.save(rsvp1);
        rsvpRepository.save(rsvp3);
        
        // Other user RSVPs to events 2 and 4 (should not affect target user's results)
        RSVP otherRsvp2 = RSVPHelperTest.createDefaultRSVP(otherUserEmail, event2Id);
        RSVP otherRsvp4 = RSVPHelperTest.createDefaultRSVP(otherUserEmail, event4Id);
        rsvpRepository.save(otherRsvp2);
        rsvpRepository.save(otherRsvp4);
        
        // Get RSVP events for target user
        List<Event> rsvpEvents = rsvpService.getRSVPEventsForUser(targetUserEmail);
        
        assertThat(rsvpEvents).hasSize(2);
        List<Long> eventIds = rsvpEvents.stream()
            .map(Event::getEventId)
            .collect(java.util.stream.Collectors.toList());
        
        assertThat(eventIds).containsExactlyInAnyOrder(event1Id, event3Id);
        assertThat(eventIds).doesNotContain(event2Id, event4Id);
        
        // Verify each event object is properly populated
        rsvpEvents.forEach(event -> {
            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getEventTitle()).isNotNull();
            assertThat(event.getClub()).isNotNull();
        });
    }

    /**
     * @brief Tests retrieving RSVP events for a user with none returns empty list.
     */
    @Test
    void getRSVPEventsForUser_shouldReturnEmptyList_whenUserHasNoRSVPs() {
        String userWithNoRSVPs = "norsvp@rmit.edu.au";
        List<Event> rsvpEvents = rsvpService.getRSVPEventsForUser(userWithNoRSVPs);
        assertThat(rsvpEvents).isEmpty();
    }

    /**
     * @brief Tests complete RSVP flow including event retrieval after toggles.
     */
    @Test
    void toggleRSVP_shouldReflectChangesInGetRSVPEventsForUser_whenFlowExecuted() {
        String userEmail = "integration@rmit.edu.au";
        
        // Initially no RSVP events
        List<Event> initialEvents = rsvpService.getRSVPEventsForUser(userEmail);
        assertThat(initialEvents).isEmpty();
        
        // RSVP to first event
        rsvpService.toggleRSVP(userEmail, event1Id);
        List<Event> eventsAfterFirst = rsvpService.getRSVPEventsForUser(userEmail);
        assertThat(eventsAfterFirst).hasSize(1);
        assertThat(eventsAfterFirst.get(0).getEventId()).isEqualTo(event1Id);
        
        // RSVP to second event
        rsvpService.toggleRSVP(userEmail, event2Id);
        List<Event> eventsAfterSecond = rsvpService.getRSVPEventsForUser(userEmail);
        assertThat(eventsAfterSecond).hasSize(2);
        assertThat(eventsAfterSecond.stream().map(Event::getEventId))
            .containsExactlyInAnyOrder(event1Id, event2Id);
        
        // Un-RSVP from first event
        rsvpService.toggleRSVP(userEmail, event1Id);
        List<Event> eventsAfterRemoval = rsvpService.getRSVPEventsForUser(userEmail);
        assertThat(eventsAfterRemoval).hasSize(1);
        assertThat(eventsAfterRemoval.get(0).getEventId()).isEqualTo(event2Id);
    }
}