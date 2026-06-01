package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Feedback entities.
 * 
 * @author Yunie Cho
 * @version 1.0
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    /**
     * @brief Finds all Feedback entries for a specific event ID.
     * 
     * @param eventId the unique identifier of the event
     * @return a list of {@link Feedback} objects associated with the given event ID
     */
    List<Feedback> findByEventId(Long eventId);
    
    /**
     * @brief Finds all Feedback entries for a specific user email.
     * 
     * @param userEmail the unique email of the user
     * @return a list of {@link Feedback} objects associated with the given user email
     */
    List<Feedback> findByUserEmail(String userEmail);
    
    /**
     * @brief Checks if feedback exists for a specific event ID and user email.
     * 
     * @param eventId the unique identifier of the event
     * @param userEmail the unique email of the user
     * @return true if feedback exists, false otherwise
     */
    boolean existsByEventIdAndUserEmail(Long eventId, String userEmail);


    /**
     * @brief Finds all Feedback entries for a specific event ID and user email.
     * 
     * @param eventId the unique identifier of the event
     * @param userEmail the unique email of the user
     * @return a list of {@link Feedback} objects associated with the given event ID and user email
     */
    List<Feedback> findByEventIdAndUserEmail(Long eventId, String userEmail);

}