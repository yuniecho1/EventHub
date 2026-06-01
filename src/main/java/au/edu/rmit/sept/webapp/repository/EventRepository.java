package au.edu.rmit.sept.webapp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.Event;
import jakarta.transaction.Transactional;


/**
 * @brief Repository interface for {@link Event} entities.
 * Extends JpaRepository to provide CRUD operations and
 * additional query methods for Event objects.
 * 
 * @author Lucas Aponso
 * @version 1.0
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * 
     * @brief Finds a list of events by the given club ID and event title.
     * This method is used to check for existing events with the same title
     * within a specific club to prevent duplicates.
     *
     * @param clubId the ID of the club to filter events by
     * @param eventTitle the title of the event to filter by
     * @return a list of matching {@link Event} objects, or an empty list if none found
     */
    List<Event> findByClubClubIdAndEventTitle(Long clubId, String eventTitle);


    /**
     * @brief Retrieves all {@link Event} entities from the database,
     * ordered by their event date in ascending order (earliest first).
     *
     * @return List of {@link Event} objects sorted by eventDate ascending.
     */
    List<Event> findAllByOrderByEventDateAsc();


    /**
     * @brief Finds events by their unique event ID.
     * 
     * @param eventId the unique identifier of the event
     * @return a list of {@link Event} objects matching the given event ID
     */
    Event findByEventId(Long eventId);

    /**
     * @brief Deletes an event by its unique event ID.
     * 
     * @param eventId the unique identifier of the event to delete
     * @return the number of entities deleted
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Event e WHERE e.eventId = :eventId")
    int deleteByEventId(@Param("eventId") Long eventId);

    /**
     * @brief Finds upcoming events starting from the given date
     * 
     * @param date the date from which to find upcoming events
     * @return List of upcoming events ordered by date
     */
    List<Event> findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate date);

    /**
     * @brief Finds the first 5 upcoming events starting from the given date
     * 
     * @param date the date from which to find upcoming events
     * @param pageable pagination information to limit results
     * @return Page of upcoming events ordered by date
     */
    @Query("SELECT e FROM Event e WHERE e.eventDate >= :date ORDER BY e.eventDate ASC")
    List<Event> findFirst5ByEventDateGreaterThanEqualOrderByEventDateAsc(@Param("date") LocalDate date, Pageable pageable);

    /**
     * @brief Searches for events where the title, description, tag, or location
     * contains the given query string (case-insensitive).
     *
     * @param query the search string
     * @return a list of {@link Event} objects matching the query
     */
    @Query("SELECT e FROM Event e " +
           "WHERE LOWER(e.eventTitle) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "   OR LOWER(e.eventDes) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "   OR LOWER(e.eventTag) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "   OR LOWER(e.location) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Event> searchEvents(@Param("query") String query);

    /**
     * @brief Retrieves all distinct event tags from the database.
     *
     * @return a list of unique tags as {@link String}
     */
    @Query("SELECT DISTINCT e.eventTag FROM Event e")
    List<String> findAllDistinctTags();


    /**
     * @brief Retrieves all distinct event that will occur by the given date.
     *
     * @return a list of unique events
     */
    List<Event> findByEventDate(LocalDate tomz);
}
