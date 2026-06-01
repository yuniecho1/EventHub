package au.edu.rmit.sept.webapp.Event;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import au.edu.rmit.sept.webapp.Club.ClubHelperTest;
import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.ClubRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;

/**
 * @brief Integration test class for {@link EventRepository}.
 *        Verifies CRUD operations, searching, and sorting behavior.
 * 
 * @author Lucas
 * @version 1.2
 */
@SpringBootTest
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ClubRepository clubRepository;

    private List<Event> hardcodedEvents;

    private Club createAndSaveTestClub(String clubName) {
        return clubRepository.save(ClubHelperTest.createDefaultClub(clubName));
    }

    @BeforeEach
    public void setup() {
        eventRepository.deleteAll();
        clubRepository.deleteAll();

        Club club1 = createAndSaveTestClub("Test Club 1");
        Club club2 = createAndSaveTestClub("Test Club 2");
        Club club3 = createAndSaveTestClub("Test Club 3");

        hardcodedEvents = Arrays.asList(
            EventHelperTest.createEvent("Event A", LocalDate.of(2025, 1, 1), 
                club1, "Desc A", "Location A", "10", "TagA"),
            EventHelperTest.createEvent("Event B", LocalDate.of(2025, 2, 1), 
                club2, "Desc B", "Location B", "20", "TagB"),
            EventHelperTest.createEvent("Event C", LocalDate.of(2025, 3, 1), 
                club3, "Desc C", "Location C", "30", "TagC")
        );
        
        eventRepository.saveAll(hardcodedEvents);
    }

    /**
     * @brief Tests saving and retrieving an event by ID.
     *
     * The test:
     * - Saves an event
     * - Finds it by its generated ID
     * - Verifies the event title and club ID
     */
    @Test
    void saveEvent_shouldBeRetrievable_whenSaved() {
        Event newEvent = hardcodedEvents.get(0);
        Event savedEvent = eventRepository.save(newEvent);

        Optional<Event> found = eventRepository.findById(savedEvent.getEventId());

        assertThat(found).isPresent();
        assertThat(found.get().getEventTitle()).isEqualTo("Event A");
        assertThat(found.get().getClub().getClubId()).isEqualTo(newEvent.getClub().getClubId());
    }

    /**
     * @brief Tests retrieving all events sorted by date ascending.
     */
    @Test
    void findAllEvents_shouldReturnSortedList_whenCalled() {
        List<Event> eventList = eventRepository.findAllByOrderByEventDateAsc();

        assertThat(eventList).isNotEmpty();
        assertThat(eventList).hasSize(hardcodedEvents.size());

        for (int i = 0; i < eventList.size() - 1; i++) {
            assertThat(eventList.get(i).getEventDate())
                .isBeforeOrEqualTo(eventList.get(i + 1).getEventDate());
        }
    }

    /**
     * @brief Tests finding an event by existing event ID.
     */
    @Test
    void findByEventId_shouldReturnEvent_whenExists() {
        Event newEvent = hardcodedEvents.get(0);
        Event savedEvent = eventRepository.save(newEvent);

        Event foundEvent = eventRepository.findByEventId(savedEvent.getEventId());

        assertThat(foundEvent).isNotNull();
        assertThat(foundEvent.getEventTitle()).isEqualTo("Event A");
        assertThat(foundEvent.getClub().getClubId()).isEqualTo(newEvent.getClub().getClubId());
    }

    /**
     * @brief Tests finding an event by non-existent ID returns null.
     */
    @Test
    void findByEventId_shouldReturnNull_whenDoesNotExist() {
        Event foundEvent = eventRepository.findByEventId(999L);
        assertThat(foundEvent).isNull();
    }

    /**
     * @brief Tests deleting an event by ID.
     */
    @Test
    void deleteByEventId_shouldRemoveEvent_whenExists() {
        Event newEvent = hardcodedEvents.get(0);
        Event savedEvent = eventRepository.save(newEvent);
        Long eventId = savedEvent.getEventId();

        int deletedCount = eventRepository.deleteByEventId(eventId);

        assertThat(deletedCount).isEqualTo(1);
        assertThat(eventRepository.findByEventId(eventId)).isNull();
    }

    /**
     * @brief Tests deleting a non-existent event returns zero.
     */
    @Test
    void deleteByEventId_shouldReturnZero_whenDoesNotExist() {
        int deletedCount = eventRepository.deleteByEventId(999L);
        assertThat(deletedCount).isEqualTo(0);
    }

    /**
     * @brief Tests finding event by club ID and title.
     */
    @Test
    void findByClubIdAndEventTitle_shouldReturnCorrectEvent_whenExists() {
        Club club = createAndSaveTestClub("Test Club 5");
        Event event = EventHelperTest.createEventForClub("Club Event", LocalDate.now().plusDays(1), club);
        eventRepository.save(event);

        List<Event> found = eventRepository.findByClubClubIdAndEventTitle(club.getClubId(), "Club Event");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getEventTitle()).isEqualTo("Club Event");
        assertThat(found.get(0).getClub().getClubId()).isEqualTo(club.getClubId());
    }

    /**
     * @brief Tests finding event by club ID and title that does not exist.
     */
    @Test
    void findByClubIdAndEventTitle_shouldReturnEmpty_whenNonexistent() {
        Club club = createAndSaveTestClub("Test Club 999");
        List<Event> found = eventRepository.findByClubClubIdAndEventTitle(club.getClubId(), "Nonexistent");
        assertThat(found).isEmpty();
    }

    /**
     * @brief Tests creating multiple events for a club and verifying retrieval by title.
     */
    @Test
    void createMultipleEvents_shouldBeRetrievableByTitle_whenSaved() {
        Club club = createAndSaveTestClub("Test Club 10");
        List<Event> events = EventHelperTest.createMultipleEventsForClub(club, "ClubMulti", 5);
        eventRepository.saveAll(events);

        List<Event> found = eventRepository.findByClubClubIdAndEventTitle(club.getClubId(), "ClubMulti 3");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getEventTitle()).isEqualTo("ClubMulti 3");
        assertThat(found.get(0).getClub().getClubId()).isEqualTo(club.getClubId());
    }

    /**
     * @brief Tests searching events by title, description, tag, and location.
     */
    @Test
    void searchEvents_shouldMatchTitleDescriptionTagLocation_whenQueryMatches() {
        Club club = createAndSaveTestClub("Test Club Search");
        Event e1 = EventHelperTest.createEvent("Alpha Event", LocalDate.now().plusDays(1), club, "Desc Alpha", "Loc1", "10", "TagX");
        Event e2 = EventHelperTest.createEvent("Beta Event", LocalDate.now().plusDays(2), club, "Desc Beta", "Loc2", "20", "TagY");
        eventRepository.saveAll(Arrays.asList(e1, e2));

        assertThat(eventRepository.searchEvents("Alpha")).extracting(Event::getEventTitle).contains("Alpha Event");
        assertThat(eventRepository.searchEvents("Desc Beta")).extracting(Event::getEventDes).contains("Desc Beta");
        assertThat(eventRepository.searchEvents("TagX")).extracting(Event::getEventTag).contains("TagX");
        assertThat(eventRepository.searchEvents("Loc2")).extracting(Event::getLocation).contains("Loc2");
    }

    /**
     * @brief Tests that empty or null search query returns all events.
     */
    @Test
    void searchEvents_shouldReturnAll_whenQueryIsEmptyOrNull() {
        List<Event> allEvents = eventRepository.searchEvents("");
        assertThat(allEvents).hasSize(hardcodedEvents.size());
    }

    /**
     * @brief Tests that event search is case-insensitive.
     */
    @Test
    void searchEvents_shouldBeCaseInsensitive_whenQueryDiffersInCase() {
        Club club = createAndSaveTestClub("Test Club Case");
        Event e = EventHelperTest.createEvent("CaseTest", LocalDate.now().plusDays(1), club, "descCase", "locCase", "10", "tagCase");
        eventRepository.save(e);

        assertThat(eventRepository.searchEvents("casetest"))
            .hasSize(1).extracting(Event::getEventTitle).contains("CaseTest");
        assertThat(eventRepository.searchEvents("DESCCASE"))
            .hasSize(1).extracting(Event::getEventDes).contains("descCase");
    }
}