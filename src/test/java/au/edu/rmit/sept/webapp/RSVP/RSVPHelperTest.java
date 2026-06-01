package au.edu.rmit.sept.webapp.RSVP;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import au.edu.rmit.sept.webapp.model.RSVP;

/**
 * @brief Helper class to create {@link RSVP} instances for testing purposes.
 * Provides utility methods to generate RSVP objects with various configurations
 * for comprehensive testing scenarios.
 * 
 * @author Agampreet Singh
 * @version 1.0
 */
public class RSVPHelperTest {

    /**
     * Creates a test {@link RSVP} object with the given parameters.
     *
     * @param userEmail the user's email address
     * @param eventId the event ID
     * @param timestamp the RSVP timestamp
     * @return a fully constructed {@link RSVP} object
     */
    public static RSVP createRSVP(String userEmail, Long eventId, LocalDateTime timestamp) {
        RSVP rsvp = new RSVP();
        rsvp.setUserEmail(userEmail);
        rsvp.setEventId(eventId);
        rsvp.setRsvpTimestamp(timestamp);
        return rsvp;
    }

    /**
     * Creates a default RSVP with common dummy values.
     *
     * @param userEmail the user's email address
     * @param eventId the event ID
     * @return a default {@link RSVP} with current timestamp
     */
    public static RSVP createDefaultRSVP(String userEmail, Long eventId) {
        return new RSVP(userEmail, eventId);
    }

    /**
     * Creates multiple RSVPs for the same event with different users.
     *
     * @param eventId the event ID
     * @param baseEmail base email for users (will append numbers)
     * @param count number of RSVPs to create
     * @return a list of {@link RSVP} objects for the same event
     */
    public static List<RSVP> createMultipleRSVPsForEvent(Long eventId, String baseEmail, int count) {
        List<RSVP> rsvps = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String userEmail = baseEmail.replace("@", i + "@");
            RSVP rsvp = createDefaultRSVP(userEmail, eventId);
            rsvps.add(rsvp);
        }
        return rsvps;
    }

    /**
     * Creates multiple RSVPs for the same user across different events.
     *
     * @param userEmail the user's email address
     * @param baseEventId base event ID (will increment for each RSVP)
     * @param count number of RSVPs to create
     * @return a list of {@link RSVP} objects for the same user
     */
    public static List<RSVP> createMultipleRSVPsForUser(String userEmail, Long baseEventId, int count) {
        List<RSVP> rsvps = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            RSVP rsvp = createDefaultRSVP(userEmail, baseEventId + i);
            rsvps.add(rsvp);
        }
        return rsvps;
    }

    /**
     * Creates an RSVP with null fields for testing validation.
     *
     * @param nullField the field to set as null ("userEmail", "eventId", "timestamp")
     * @return an {@link RSVP} with the specified null field
     */
    public static RSVP createRSVPWithNullField(String nullField) {
        RSVP rsvp = createDefaultRSVP("test@example.com", 1L);
        switch (nullField.toLowerCase()) {
            case "useremail" -> rsvp.setUserEmail(null);
            case "eventid" -> rsvp.setEventId(null);
            case "timestamp" -> rsvp.setRsvpTimestamp(null);
        }
        return rsvp;
    }

    /**
     * Creates a duplicate RSVP for testing unique constraint violations.
     *
     * @param existingRSVP an existing RSVP to duplicate
     * @return a new {@link RSVP} with the same userEmail and eventId
     */
    public static RSVP createDuplicateRSVP(RSVP existingRSVP) {
        return createDefaultRSVP(existingRSVP.getUserEmail(), existingRSVP.getEventId());
    }
}