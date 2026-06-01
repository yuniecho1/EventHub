package au.edu.rmit.sept.webapp.Event;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Club;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief Helper class to create {@link Event} instances for testing purposes.
 */
public class EventHelperTest {

    /**
     * Creates a test {@link Event} object with the given parameters.
     *
     * @param title    the event title
     * @param date     the event date
     * @param club     the club object
     * @param desc     the event description
     * @param location the event location
     * @param price    the event price
     * @param tag      the event tag
     * @return a fully constructed {@link Event} object
     */
    public static Event createEvent(String title, LocalDate date, Club club,
                                    String desc, String location, String price, String tag) {
        Event event = new Event();
        event.setEventTitle(title);
        event.setEventDate(date);
        event.setClub(club);
        event.setEventDes(desc);
        event.setLocation(location);
        event.setPrice(price);
        event.setEventTag(tag);
        return event;
    }

    /**
     * Creates a default test club object.
     * @param clubId the club ID
     * @return a test {@link Club} object
     */
    public static Club createTestClub(Long clubId) {
        Club club = new Club();
        club.setClubId(clubId);
        club.setClubName("Test Club " + clubId);
        // Note: Only setting fields that exist in the Club entity
        return club;
    }

    /**
     * Creates a default event with common dummy values.
     * @param title the event title
     * @param date the event date
     * @return a default {@link Event} with preset dummy fields
     */
    public static Event createDefaultEvent(String title, LocalDate date) {
        Club defaultClub = createTestClub(1L);
        return createEvent(
            title,
            date,
            defaultClub,
            "Sample Description",
            "Sample Location",
            "Free",
            "SampleTag"
        );
    }
    
    /**
     * Creates an event with specific club for testing club-specific operations.
     * @param title the event title
     * @param date the event date
     * @param club the specific club
     * @return an {@link Event} with the specified club
     */
    public static Event createEventForClub(String title, LocalDate date, Club club) {
        return createEvent(
            title,
            date,
            club,
            "Description for " + title,
            "Location for " + title,
            "Free",
            "TestTag"
        );
    }
    
    /**
     * Creates an event with a future date for testing valid event creation.
     * @param title the event title
     * @param daysFromNow number of days from today
     * @return an {@link Event} with a future date
     */
    public static Event createFutureEvent(String title, int daysFromNow) {
        return createDefaultEvent(title, LocalDate.now().plusDays(daysFromNow));
    }

    /**
     * Creates an event with a past date for testing validation.
     * @param title the event title
     * @param daysAgo number of days ago
     * @return an {@link Event} with a past date
     */
    public static Event createPastEvent(String title, int daysAgo) {
        return createDefaultEvent(title, LocalDate.now().minusDays(daysAgo));
    }

    /**
     * Creates an event with null fields for testing validation.
     * @param nullField the field to set as null
     * @return an {@link Event} with the specified null field
     */
    public static Event createEventWithNullField(String nullField) {
        Event event = createDefaultEvent("Test Event", LocalDate.now().plusDays(1));
        
        switch (nullField.toLowerCase()) {
            case "title":
                event.setEventTitle(null);
                break;
            case "date":
                event.setEventDate(null);
                break;
            case "clubid":
            case "club":
                event.setClub(null);
                break;
            case "description":
                event.setEventDes(null);
                break;
            case "location":
                event.setLocation(null);
                break;
            case "price":
                event.setPrice(null);
                break;
            case "tag":
                event.setEventTag(null);
                break;
        }
        
        return event;
    }

    /**
     * Creates multiple events for the same club with different titles.
     * @param club the club
     * @param baseTitle base title for events
     * @param count number of events to create
     * @return a list of {@link Event} objects for the same club
     */
    public static List<Event> createMultipleEventsForClub(Club club, String baseTitle, int count) {
        List<Event> events = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            Event event = createEventForClub(
                baseTitle + " " + i,
                LocalDate.now().plusDays(i),
                club
            );
            events.add(event);
        }
        
        return events;
    }

    /**
     * Overloaded method for backward compatibility - creates multiple events with a club ID.
     * @param clubId the club ID
     * @param baseTitle base title for events
     * @param count number of events to create
     * @return a list of {@link Event} objects for the same club
     */
    public static List<Event> createMultipleEventsForClub(Long clubId, String baseTitle, int count) {
        Club club = createTestClub(clubId);
        return createMultipleEventsForClub(club, baseTitle, count);
    }
}