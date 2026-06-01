package au.edu.rmit.sept.webapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * @brief Represents an RSVP response for an event by a user.
 * This entity stores RSVP details including the user's email, event ID,
 * and timestamp of when the RSVP was created.
 * The existence of a record indicates the user is attending the event.
 * 
 * @author Agampreet Singh
 * @version 1.0
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userEmail", "eventId"}))
public class RSVP {

    /**
     * Unique identifier for the RSVP.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rsvpId;

    /**
     * Email of the user making the RSVP.
     */
    @Column(nullable = false)
    private String userEmail;

    /**
     * The ID of the event being RSVP'd to.
     */
    @Column(nullable = false)
    private Long eventId;

    /**
     * Timestamp of when the RSVP was created.
     * Automatically set to current timestamp on creation.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime rsvpTimestamp;

    /**
     * Default constructor.
     * Sets the timestamp to current time.
     */
    public RSVP() {
        this.rsvpTimestamp = LocalDateTime.now();
    }

    /**
     * Constructor with parameters.
     * 
     * @param userEmail the email of the user
     * @param eventId the ID of the event
     */
    public RSVP(String userEmail, Long eventId) {
        this.userEmail = userEmail;
        this.eventId = eventId;
        this.rsvpTimestamp = LocalDateTime.now();
    }

    // Getters and Setters

    /**
     * Returns the RSVP ID.
     * 
     * @return the RSVP ID
     */
    public Long getRsvpId() {
        return rsvpId;
    }

    /**
     * Sets the RSVP ID.
     * 
     * @param rsvpId the RSVP ID to set
     */
    public void setRsvpId(Long rsvpId) {
        this.rsvpId = rsvpId;
    }

    /**
     * Returns the user email.
     * 
     * @return the user email
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Sets the user email.
     * 
     * @param userEmail the user email to set
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Returns the event ID.
     * 
     * @return the event ID
     */
    public Long getEventId() {
        return eventId;
    }

    /**
     * Sets the event ID.
     * 
     * @param eventId the event ID to set
     */
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * Returns the RSVP timestamp.
     * 
     * @return the RSVP timestamp
     */
    public LocalDateTime getRsvpTimestamp() {
        return rsvpTimestamp;
    }

    /**
     * Sets the RSVP timestamp.
     * 
     * @param rsvpTimestamp the RSVP timestamp to set
     */
    public void setRsvpTimestamp(LocalDateTime rsvpTimestamp) {
        this.rsvpTimestamp = rsvpTimestamp;
    }

    /**
     * Called before the entity is persisted to the database.
     * Ensures the timestamp is set if not already provided.
     */
    @PrePersist
    protected void onCreate() {
        if (rsvpTimestamp == null) {
            rsvpTimestamp = LocalDateTime.now();
        }
    }
}