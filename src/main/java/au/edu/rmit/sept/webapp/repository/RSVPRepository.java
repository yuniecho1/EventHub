package au.edu.rmit.sept.webapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.RSVP;

/**
 * @brief Repository interface for {@link RSVP} entities.
 * Extends JpaRepository to provide CRUD operations and
 * additional query methods for RSVP objects.
 * 
 * @author Agampreet Singh
 * @version 1.0
 */
@Repository
public interface RSVPRepository extends JpaRepository<RSVP, Long> {

    /**
     * @brief Finds all RSVP entries for a specific event ID.
     * 
     * @param eventId the unique identifier of the event
     * @return a list of {@link RSVP} objects associated with the given event ID
     */
    List<RSVP> findAllByEventId(Long eventId);


    /**
     * @brief Finds all RSVP entries for a specific user.
     * 
     * @param userEmail the unique email of the user
     * @return a list of {@link RSVP} objects associated with the given user
     */
    List<RSVP> findAllByUserEmail(String userEmail);

    /**
     * @brief Finds an RSVP by user email and event ID.
     * 
     * @param userEmail the user's email
     * @param eventId the event ID
     * @return the RSVP if found, null otherwise
     */
    RSVP findByUserEmailAndEventId(String userEmail, Long eventId);


    List<RSVP> findByEventId(Long eventId);

}
