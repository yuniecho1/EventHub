package au.edu.rmit.sept.webapp.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * @brief Represents an event organized by a club.
 * This entity stores event details including its title, description,
 * date, location, pricing, and categorization tags.
 * Note: Club relationship is currently represented by a club ID. 
 * This should be updated to a proper {@code @ManyToOne} association 
 * with a {@code Club} entity once club functionality is implemented.
 * 
 * @author Lucas Aponso
 * @version 1.0
 */
@Entity
public class Event {

    /**
     * Unique identifier for the event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    /**
     * The club hosting the event.
     * {@code @ManyToOne} relationship with proper JPA annotations in future.
     */
    @ManyToOne
    @JoinColumn(name = "clubId", nullable = false)
    private Club club;

    /**
     * Title or name of the event.
     */
    private String eventTitle;

    /**
     * Description of the event.
     */
    private String eventDes;

    /**
     * Date on which the event will occur.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventDate;

    /**
     * Location where the event will take place.
     */
    private String location;

    /**
     * Pricing information for the event.
     */
    private String price;

    /**
     * Tag or category associated with the event.
     */
    private String eventTag;

    // Getters and Setters

    /**
     * Returns the unique event ID.
     * 
     * @return the event ID
     */
    public Long getEventId() {
        return eventId;
    }

    /**
     * Sets the unique event ID.
     * 
     * @param eventId the event ID to set
     */
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * Returns the club hosting the event.
     * 
     * @return the club
     */
    public Club getClub() {
        return club;
    }

    /**
     * Sets the club hosting the event.
     * 
     * @param club the club to set
     */
    public void setClub(Club club) {
        this.club = club;
    }

    /**
     * Returns the event title.
     * 
     * @return the event title
     */
    public String getEventTitle() {
        return eventTitle;
    }

    /**
     * Sets the event title.
     * 
     * @param eventTitle the event title to set
     */
    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    /**
     * Returns the event description.
     * 
     * @return the event description
     */
    public String getEventDes() {
        return eventDes;
    }

    /**
     * Sets the event description.
     * 
     * @param eventDes the event description to set
     */
    public void setEventDes(String eventDes) {
        this.eventDes = eventDes;
    }

    /**
     * Returns the date of the event.
     * 
     * @return the event date
     */
    public LocalDate getEventDate() {
        return eventDate;
    }

    /**
     * Sets the date of the event.
     * 
     * @param eventDate the event date to set
     */
    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    /**
     * Returns the event location.
     * 
     * @return the event location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the event location.
     * 
     * @param location the event location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the price information for the event.
     * 
     * @return the price
     */
    public String getPrice() {
        return price;
    }

    /**
     * Sets the price information for the event.
     * 
     * @param price the price to set
     */
    public void setPrice(String price) {
        this.price = price;
    }

    /**
     * Returns the event tag or category.
     * 
     * @return the event tag
     */
    public String getEventTag() {
        return eventTag;
    }

    /**
     * Sets the event tag or category.
     * 
     * @param eventTag the event tag to set
     */
    public void setEventTag(String eventTag) {
        this.eventTag = eventTag;
    }
}