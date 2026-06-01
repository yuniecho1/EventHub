package au.edu.rmit.sept.webapp.Event;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.Club.ClubHelperTest;
import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.ClubRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.service.EventService;

/**
 * @brief Integration test class for {@link EventService}.
 * 
 * Verifies the correctness of:
 * - Creating, editing, and deleting events
 * - Retrieving events by ID or as a list
 * - Searching events
 * - Handling invalid or duplicate inputs
 * 
 * All tests are transactional to rollback after each test.
 */
@SpringBootTest
@Transactional
public class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ClubRepository clubRepository;

    private Club testClub;

    /**
     * @brief Helper method to create and persist a test club.
     */
    private Club createAndSaveTestClub(String clubName) {
        return clubRepository.save(ClubHelperTest.createDefaultClub(clubName));
    }

    @BeforeEach
    public void setup() {
        eventRepository.deleteAll();
        clubRepository.deleteAll();
        testClub = createAndSaveTestClub("Test Club 1");
    }

    /**
     * @brief Tests saving a valid {@link Event} and verifying it is persisted correctly.
     * 
     * The test:
     * - Creates a valid event with future date
     * - Saves it using EventService
     * - Asserts that it has a generated ID and correct title
     */
    @Test
    void createEvent_shouldBeSaved_whenValid() {
        Event event = EventHelperTest.createEvent("Tech Talk", LocalDate.now().plusDays(10),
            testClub, "Description", "Location", "Free", "Tech");
        Event saved = eventService.createEvent(event);

        assertThat(saved).isNotNull();
        assertThat(saved.getEventId()).isNotNull();
        assertThat(saved.getEventTitle()).isEqualTo("Tech Talk");
    }

    /**
     * @brief Tests that creating an event with a null title throws {@link IllegalArgumentException}.
     */
    @Test
    void createEvent_shouldThrowException_whenTitleIsNull() {
        Event event = EventHelperTest.createEvent(null, LocalDate.now().plusDays(5),
            testClub, "Description", "Location", "Free", "Tag");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(event));

        assertThat(exception.getMessage()).contains("Event title cannot be empty");
    }

    /**
     * @brief Tests that creating an event with a past date throws {@link IllegalArgumentException}.
     */
    @Test
    void createEvent_shouldThrowException_whenDateIsInPast() {
        Event event = EventHelperTest.createEvent("Old Event", LocalDate.now().minusDays(1),
            testClub, "Description", "Location", "Free", "Tag");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(event));

        assertThat(exception.getMessage()).contains("Event date cannot be in the past");
    }

    /**
     * @brief Tests that creating a duplicate event (same title for the same club) throws exception.
     */
    @Test
    void createEvent_shouldThrowException_whenDuplicateTitle() {
        Event event1 = EventHelperTest.createEvent("Duplicate Event", LocalDate.now().plusDays(5),
            testClub, "Description", "Location", "Free", "Tag");
        Event event2 = EventHelperTest.createEvent("Duplicate Event", LocalDate.now().plusDays(7),
            testClub, "Description", "Location", "Free", "Tag");

        eventService.createEvent(event1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(event2));
        assertThat(exception.getMessage()).contains("already exists");
    }

    /**
     * @brief Tests that {@link EventService#getEvents} returns a list sorted by event date ascending.
     */
    @Test
    void getEvents_shouldReturnSortedList() {
        Event event1 = EventHelperTest.createEvent("Early Event", LocalDate.now().plusDays(1),
            testClub, "Description", "Location", "Free", "Tag");
        Event event2 = EventHelperTest.createEvent("Later Event", LocalDate.now().plusDays(10),
            testClub, "Description", "Location", "Free", "Tag");

        eventService.createEvent(event2);
        eventService.createEvent(event1);

        List<Event> events = eventService.getEvents();
        assertThat(events).hasSize(2);
        assertThat(events.get(0).getEventDate()).isBefore(events.get(1).getEventDate());
    }

    /**
     * @brief Tests retrieving an event by its ID.
     */
    @Test
    void getEventById_shouldReturnEvent_whenExists() {
        Event event = EventHelperTest.createEvent("Test Event", LocalDate.now().plusDays(5),
            testClub, "Description", "Location", "Free", "Tag");
        Event savedEvent = eventService.createEvent(event);

        Event foundEvent = eventService.getEventById(savedEvent.getEventId());
        assertThat(foundEvent).isNotNull();
        assertThat(foundEvent.getEventId()).isEqualTo(savedEvent.getEventId());
        assertThat(foundEvent.getEventTitle()).isEqualTo("Test Event");
    }

    /**
     * @brief Tests retrieving a non-existent event by ID returns null.
     */
    @Test
    void getEventById_shouldReturnNull_whenNotFound() {
        Event foundEvent = eventService.getEventById(999L);
        assertThat(foundEvent).isNull();
    }

    /**
     * @brief Tests deleting an existing event by ID.
     */
    @Test
    void deleteEventById_shouldReturnTrue_whenDeleted() {
        Event event = EventHelperTest.createEvent("Event to Delete", LocalDate.now().plusDays(3),
            testClub, "Description", "Location", "Free", "Tag");
        Event savedEvent = eventService.createEvent(event);

        boolean deleted = eventService.deleteEventById(savedEvent.getEventId());
        assertThat(deleted).isTrue();
        assertThat(eventService.getEventById(savedEvent.getEventId())).isNull();
    }

    /**
     * @brief Tests deleting a non-existent event by ID returns false.
     */
    @Test
    void deleteEventById_shouldReturnFalse_whenNotFound() {
        boolean deleted = eventService.deleteEventById(999L);
        assertThat(deleted).isFalse();
    }

    /**
     * @brief Tests editing an existing event successfully updates all fields.
     */
    @Test
    void editEventById_shouldUpdateFields_whenEventExists() {
        Event originalEvent = EventHelperTest.createEvent("Original Event", LocalDate.now().plusDays(5),
            testClub, "Description", "Location", "Free", "Tag");
        Event savedEvent = eventService.createEvent(originalEvent);

        Club updatedClub = createAndSaveTestClub("Test Club 2");
        Event updatedEvent = EventHelperTest.createEvent(
            "Updated Event",
            LocalDate.now().plusDays(10),
            updatedClub,
            "Updated Description",
            "Updated Location",
            "25",
            "UpdatedTag"
        );

        Event editedEvent = eventService.editEventById(savedEvent.getEventId(), updatedEvent);
        assertThat(editedEvent).isNotNull();
        assertThat(editedEvent.getEventId()).isEqualTo(savedEvent.getEventId());
        assertThat(editedEvent.getEventTitle()).isEqualTo("Updated Event");
        assertThat(editedEvent.getClub()).isNotNull();
        assertThat(editedEvent.getEventDes()).isEqualTo("Updated Description");
        assertThat(editedEvent.getLocation()).isEqualTo("Updated Location");
        assertThat(editedEvent.getPrice()).isEqualTo("25");
        assertThat(editedEvent.getEventTag()).isEqualTo("UpdatedTag");
    }

    /**
     * @brief Tests editing a non-existent event throws {@link IllegalArgumentException}.
     */
    @Test
    void editEventById_shouldThrowException_whenNotFound() {
        Event updateEvent = EventHelperTest.createEvent("Non-existent Event", LocalDate.now().plusDays(5),
            testClub, "Description", "Location", "Free", "Tag");

        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> eventService.editEventById(999L, updateEvent));
        assertThat(exception.getMessage()).contains("Event with ID 999 not found");
    }

    /**
     * @brief Tests editing an event with null fields throws {@link IllegalArgumentException}.
     */
    @Test
    void editEventById_shouldThrowException_whenFieldsNull() {
        Event originalEvent = EventHelperTest.createEvent("Original Event", LocalDate.now().plusDays(5),
            testClub, "Description", "Location", "Free", "Tag");
        Event savedEvent = eventService.createEvent(originalEvent);

        Event updateEvent = new Event();
        updateEvent.setEventTitle("Updated Title"); // leave other fields null

        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> eventService.editEventById(savedEvent.getEventId(), updateEvent));
        assertThat(exception.getMessage()).contains("cannot be empty");
    }

    /**
     * @brief Tests retrieving upcoming events from today onwards returns correct list.
     */
    @Test
    void getUpcomingEventsForDashboard_shouldReturnOnlyFutureEvents() {
        Event pastEvent1 = EventHelperTest.createEvent("Past Event 1", LocalDate.now().minusDays(5),
            testClub, "Description", "Location", "Free", "Tag");
        Event pastEvent2 = EventHelperTest.createEvent("Past Event 2", LocalDate.now().minusDays(2),
            testClub, "Description", "Location", "Free", "Tag");

        Event futureEvent1 = EventHelperTest.createEvent("Future Event 1", LocalDate.now().plusDays(1),
            testClub, "Description", "Location", "Free", "Tag");
        Event futureEvent2 = EventHelperTest.createEvent("Future Event 2", LocalDate.now().plusDays(5),
            testClub, "Description", "Location", "Free", "Tag");
        Event futureEvent3 = EventHelperTest.createEvent("Future Event 3", LocalDate.now().plusDays(10),
            testClub, "Description", "Location", "Free", "Tag");

        eventRepository.save(pastEvent1);
        eventRepository.save(pastEvent2);
        eventService.createEvent(futureEvent1);
        eventService.createEvent(futureEvent2);
        eventService.createEvent(futureEvent3);

        List<Event> upcomingEvents = eventService.getUpcomingEventsForDashboard();

        assertThat(upcomingEvents).hasSize(3);
        assertThat(upcomingEvents.get(0).getEventTitle()).isEqualTo("Future Event 1");
        assertThat(upcomingEvents.get(1).getEventTitle()).isEqualTo("Future Event 2");
        assertThat(upcomingEvents.get(2).getEventTitle()).isEqualTo("Future Event 3");

        for (Event event : upcomingEvents) {
            assertThat(event.getEventDate()).isAfterOrEqualTo(LocalDate.now());
        }
    }

    /**
     * @brief Tests getUpcomingEventsForDashboard returns empty list if no upcoming events exist.
     */
    @Test
    void getUpcomingEventsForDashboard_shouldReturnEmpty_whenNoFutureEvents() {
        Event pastEvent1 = EventHelperTest.createEvent("Past Event 1", LocalDate.now().minusDays(3),
            testClub, "Description", "Location", "Free", "Tag");
        Event pastEvent2 = EventHelperTest.createEvent("Past Event 2", LocalDate.now().minusDays(1),
            testClub, "Description", "Location", "Free", "Tag");

        eventRepository.save(pastEvent1);
        eventRepository.save(pastEvent2);

        List<Event> upcomingEvents = eventService.getUpcomingEventsForDashboard();
        assertThat(upcomingEvents).isEmpty();
    }

    /**
     * @brief Tests getUpcomingEventsForDashboard returns events sorted by date ascending.
     */
    @Test
    void getUpcomingEventsForDashboard_shouldReturnSortedByDate() {
        Event laterEvent = EventHelperTest.createEvent("Later Event", LocalDate.now().plusDays(10),
            testClub, "Description", "Location", "Free", "Tag");
        Event earlierEvent = EventHelperTest.createEvent("Earlier Event", LocalDate.now().plusDays(2),
            testClub, "Description", "Location", "Free", "Tag");
        Event middleEvent = EventHelperTest.createEvent("Middle Event", LocalDate.now().plusDays(5), 
            testClub, "Description", "Location", "Free", "Tag");

        eventService.createEvent(laterEvent);
        eventService.createEvent(earlierEvent);
        eventService.createEvent(middleEvent);

        List<Event> upcomingEvents = eventService.getUpcomingEventsForDashboard();

        assertThat(upcomingEvents).hasSize(3);
        assertThat(upcomingEvents.get(0).getEventTitle()).isEqualTo("Earlier Event");
        assertThat(upcomingEvents.get(1).getEventTitle()).isEqualTo("Middle Event");
        assertThat(upcomingEvents.get(2).getEventTitle()).isEqualTo("Later Event");

        for (int i = 0; i < upcomingEvents.size() - 1; i++) {
            assertThat(upcomingEvents.get(i).getEventDate())
                .isBeforeOrEqualTo(upcomingEvents.get(i + 1).getEventDate());
        }
    }

    /**
     * @brief Tests searching events with an empty query returns all events.
     */
    @Test
    void searchEvents_shouldReturnAll_whenQueryEmpty() {
        Event e1 = EventHelperTest.createEvent("Event1", LocalDate.now().plusDays(1),
            testClub, "Description", "Location", "Free", "Tag");
        Event e2 = EventHelperTest.createEvent("Event2", LocalDate.now().plusDays(2),
            testClub, "Description", "Location", "Free", "Tag");

        eventService.createEvent(e1);
        eventService.createEvent(e2);

        List<Event> results = eventService.searchEvents("");
        assertThat(results).hasSize(2);
    }

    /**
     * @brief Tests that searchEvents matches title, description, tag, and location correctly.
     */
    @Test
    void searchEvents_shouldMatchTitleDescriptionTagLocation() {
        Event e1 = EventHelperTest.createEvent("Java Workshop", LocalDate.now().plusDays(1),
            testClub, "Learn Java basics", "Room A", "Free", "Coding");
        Event e2 = EventHelperTest.createEvent("Python Workshop", LocalDate.now().plusDays(2),
            testClub, "Learn Python", "Room B", "Free", "Programming");
        Event e3 = EventHelperTest.createEvent("C++ Workshop", LocalDate.now().plusDays(3),
            testClub, "Advanced topics", "Room C", "Free", "Coding");

        eventService.createEvent(e1);
        eventService.createEvent(e2);
        eventService.createEvent(e3);

        List<Event> results1 = eventService.searchEvents("java");
        List<Event> results2 = eventService.searchEvents("Coding");
        List<Event> results3 = eventService.searchEvents("Room B");

        assertThat(results1).hasSize(1).extracting(Event::getEventTitle).containsExactly("Java Workshop");
        assertThat(results2).hasSize(2).extracting(Event::getEventTitle)
            .containsExactlyInAnyOrder("Java Workshop", "C++ Workshop");
        assertThat(results3).hasSize(1).extracting(Event::getEventTitle).containsExactly("Python Workshop");
    }

    /**
     * @brief Tests that searchEvents is case-insensitive.
     */
    @Test
    void searchEvents_shouldBeCaseInsensitive() {
        Event e1 = EventHelperTest.createEvent("Data Science", LocalDate.now().plusDays(1),
            testClub, "Description", "Location", "Free", "Tag");
        eventService.createEvent(e1);

        List<Event> results = eventService.searchEvents("data science");
        assertThat(results).hasSize(1).extracting(Event::getEventTitle).containsExactly("Data Science");

        results = eventService.searchEvents("DATA SCIENCE");
        assertThat(results).hasSize(1).extracting(Event::getEventTitle).containsExactly("Data Science");
    }

    /**
     * @brief Tests creating multiple events for the same club.
     */
    @Test
    void createMultipleEventsForClub_shouldSaveAllEvents() {
        Club club2 = createAndSaveTestClub("Test Club 2");
        List<Event> events = EventHelperTest.createMultipleEventsForClub(club2, "Club Event", 3);
        events.forEach(eventService::createEvent);

        List<Event> allEvents = eventService.getEvents();
        assertThat(allEvents).hasSize(3);
        assertThat(allEvents).extracting(Event::getEventTitle).containsExactly("Club Event 1", "Club Event 2", "Club Event 3");
    }

    /**
     * @brief Tests creating events with null fields using helper throws exceptions.
     */
    @Test
    void createEventWithNullFields_shouldThrowException() {
        Event eventWithNullTitle = EventHelperTest.createEventWithNullField("title");
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> eventService.createEvent(eventWithNullTitle));
        assertThat(exception.getMessage()).contains("Event title cannot be empty");

        Event eventWithNullDate = EventHelperTest.createEventWithNullField("date");
        exception = assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(eventWithNullDate));
        assertThat(exception.getMessage()).contains("Event date cannot be empty");
    }

    /**
     * @brief Tests creating an event with a null club throws {@link IllegalArgumentException}.
     */
    @Test
    void createEventWithNullClub_shouldThrowException() {
        Event event = EventHelperTest.createEventWithNullField("club");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(event));
        assertThat(exception.getMessage()).contains("Event club cannot be empty");
    }

    /**
     * @brief Tests creating events for different clubs.
     */
    @Test
    void createEventsForDifferentClubs_shouldAssignCorrectClub() {
        Club club1 = testClub;
        Club club2 = createAndSaveTestClub("Test Club 2");

        Event event1 = EventHelperTest.createEventForClub("Club 1 Event", LocalDate.now().plusDays(5), club1);
        Event event2 = EventHelperTest.createEventForClub("Club 2 Event", LocalDate.now().plusDays(6), club2);

        Event saved1 = eventService.createEvent(event1);
        Event saved2 = eventService.createEvent(event2);

        assertThat(saved1.getClub().getClubName()).isEqualTo("Test Club 1");
        assertThat(saved2.getClub().getClubName()).isEqualTo("Test Club 2");
    }
}