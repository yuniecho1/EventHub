package au.edu.rmit.sept.webapp.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.EventRepository;

/**
 * @brief Service class for managing {@link Event} entities.
 * Provides business logic for creating, retrieving, updating, and deleting events,
 * including validation and duplicate checks.
 * 
 * Handles upcoming events retrieval for dashboard and fetching distinct tags.
 * Delegates database operations to {@link EventRepository}.
 * 
 * @author Lucas
 * @version 1.0
 */
@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;
    
    /**
     * @brief Validates that all required fields of an {@link Event} are non-null.
     * 
     * Throws an exception if any required field is null.
     * 
     * @param event the event to validate
     * @throws IllegalArgumentException if any required field is null
     */
    public void validateEventNotNull(Event event) {
        if (event.getClub() == null) {
            throw new IllegalArgumentException("Event club cannot be empty");
        }
        if (event.getEventTitle() == null) {
            throw new IllegalArgumentException("Event title cannot be empty");
        }
        if (event.getEventDate() == null) {
            throw new IllegalArgumentException("Event date cannot be empty");
        }
        if (event.getEventDes() == null) {
            throw new IllegalArgumentException("Event description cannot be empty");
        }
        if (event.getLocation() == null) {
            throw new IllegalArgumentException("Location cannot be empty");
        }
        if (event.getPrice() == null) {
            throw new IllegalArgumentException("Price cannot be empty");
        }
        if (event.getEventTag() == null) {
            throw new IllegalArgumentException("Event tag cannot be empty");
        }
    }

    /**
     * @brief Validates that the price field of an {@link Event} is a valid numeric format.
     * 
     * Accepts whole numbers and decimals (e.g., "10", "10.50"). 
     * Rejects symbols, text, or negative values.
     * 
     * @param event the event to validate
     * @throws IllegalArgumentException if the price format is invalid
     */
    public String validateEventPriceFormat(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Price cannot be empty");
        }

        // Allow "Free" prices
        if (priceStr.equalsIgnoreCase("free")) {
            return "Free";
        }

        // Only allow digits and at most two decimal points
        if (!priceStr.matches("^\\d+(\\.\\d{1,2})?$")) {
            throw new IllegalArgumentException("Price must be a valid number with up to two decimal places.");
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Price must be a valid numeric value");
        }
        return "$" + String.format("%.2f", price);
    }

    /**
     * @brief Creates a new {@link Event} in the repository after validating input.
     * Ensures no duplicate event exists for the same club with the same title
     * and that the event date is not in the past.
     * 
     * @param event the event to create
     * @return the saved {@link Event} entity
     * @throws IllegalArgumentException if validation fails or a duplicate event exists
     */
    @Transactional
    public Event createEvent(Event event) {
        validateEventNotNull(event);
        event.setPrice(validateEventPriceFormat(event.getPrice()));

        if (event.getEventDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past");
        }

        List<Event> existingEvents = eventRepository.findByClubClubIdAndEventTitle(
            event.getClub().getClubId(), event.getEventTitle());
        
        if (!existingEvents.isEmpty()) {
            throw new IllegalArgumentException(
                "An event with title '" + event.getEventTitle() + "' for club ID " + event.getClub() + " already exists");
        }

        return eventRepository.save(event);
    }

    /**
     * @brief Retrieves all events sorted by date ascending.
     * 
     * @return a list of {@link Event} entities
     */
    @Transactional
    public List<Event> getEvents() {
        return eventRepository.findAllByOrderByEventDateAsc();
    }

    /**
     * @brief Searches events by query string.
     * 
     * If query is null or empty, returns all events.
     * 
     * @param query the search string
     * @return a list of {@link Event} entities matching the query
     */
    public List<Event> searchEvents(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getEvents();
        }
        return eventRepository.searchEvents(query);
    }

    /**
     * @brief Retrieves an event by its ID.
     * 
     * @param eventId the ID of the event
     * @return the {@link Event} entity with the given ID
     */
    @Transactional
    public Event getEventById(Long eventId) {
        return eventRepository.findByEventId(eventId);
    }

    /**
     * @brief Deletes an event by its ID.
     * 
     * @param eventId the ID of the event to delete
     * @return true if deletion was successful, false otherwise
     */
    @Transactional
    public boolean deleteEventById(Long eventId) {
        int deletedCount = eventRepository.deleteByEventId(eventId);
        return deletedCount > 0;
    }

    /**
     * @brief Updates an existing event by ID with new data.
     * 
     * Validates required fields and throws exception if the event does not exist.
     * 
     * @param eventId the ID of the event to edit
     * @param event the updated event data
     * @return the updated {@link Event} entity
     * @throws IllegalArgumentException if the event does not exist or validation fails
     */
    public Event editEventById(Long eventId, Event event) {
        Event existingEvent = eventRepository.findByEventId(eventId);
        if (existingEvent == null) {
            throw new IllegalArgumentException("Event with ID " + eventId + " not found");
        }
        validateEventNotNull(event);

        existingEvent.setClub(event.getClub());
        existingEvent.setEventTitle(event.getEventTitle());
        existingEvent.setEventDate(event.getEventDate());
        existingEvent.setEventDes(event.getEventDes());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setPrice(event.getPrice());
        existingEvent.setEventTag(event.getEventTag());

        return eventRepository.save(existingEvent);
    }

    /**
     * @brief Retrieves the first 5 upcoming events closest to today's date.
     * 
     * Useful for displaying events on dashboards or highlights.
     * 
     * @return a list of up to 5 upcoming {@link Event} entities
     */
    @Transactional
    public List<Event> getUpcomingEventsForDashboard() {
        return eventRepository.findFirst5ByEventDateGreaterThanEqualOrderByEventDateAsc(
            LocalDate.now(), 
            PageRequest.of(0, 5)
        );
    }

    /**
     * @brief Retrieves all distinct event tags.
     * 
     * @return a list of unique event tags as {@link String}
     */
    @Transactional
    public List<String> findAllTags() {
        return eventRepository.findAllDistinctTags();
    }
}