package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Feedback;
import au.edu.rmit.sept.webapp.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for managing Feedback entities.
 * 
 * @author Yunie Cho
 * @version 1.0
 */
@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    /**
     * @brief Validates the feedback data before saving.
     * 
     * @param feedback the Feedback object to validate
     * @return void
     * @throws IllegalArgumentException if validation fails
     */
    public void validateFeedback(Feedback feedback) {
        if (feedback.getEventId() == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        if (feedback.getUserEmail() == null || feedback.getUserEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }
        if (feedback.getFeedbackText() == null || feedback.getFeedbackText().trim().isEmpty()) {
            throw new IllegalArgumentException("Feedback text cannot be null or empty");
        }
        if (feedback.getFeedbackText().length() > 200) {
            throw new IllegalArgumentException("Feedback text cannot exceed 200 characters");
        }
        if (feedback.getRating() == null || feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    /**
     * @brief Creates a new feedback entry after validation.
     * 
     * @param feedback the Feedback object to create
     * @return the created Feedback object
     * @throws IllegalArgumentException if validation fails or user has already submitted feedback for the event
     */
    @Transactional
    public Feedback createFeedback(Feedback feedback) {
        validateFeedback(feedback);
        
        // Check if user has already submitted feedback for this event
        if (feedbackRepository.existsByEventIdAndUserEmail(feedback.getEventId(), feedback.getUserEmail())) {
            throw new IllegalArgumentException("You have already submitted feedback for this event");
        }
        
        return feedbackRepository.save(feedback);
    }

    /**
    * @brief Finds all Feedback entries for a specific event ID.
    * 
    * @param eventId the unique identifier of the event
    * @return a list of {@link Feedback} objects associated with the given event ID
    */
    public List<Feedback> getFeedbackForEvent(Long eventId) {
        return feedbackRepository.findByEventId(eventId);
    }

    /**
     * @brief Checks if a user has already submitted feedback for a specific event.
     * 
     * @param eventId the unique identifier of the event
     * @param userEmail the unique email of the user
     * @return true if feedback exists, false otherwise
     */
    public boolean hasUserSubmittedFeedback(Long eventId, String userEmail) {
        return feedbackRepository.existsByEventIdAndUserEmail(eventId, userEmail);
    }

    /**
     * @brief Finds all Feedback entries for a specific event ID and user email.
     * 
     * @param eventId the unique identifier of the event
     * @param userEmail the unique email of the user
     * @return a list of {@link Feedback} objects associated with the given event ID and user email
     */
    public List<Feedback> getFeedbackForUserAndEvent(Long eventId, String userEmail) {
        return feedbackRepository.findByEventIdAndUserEmail(eventId, userEmail);
    }
}
