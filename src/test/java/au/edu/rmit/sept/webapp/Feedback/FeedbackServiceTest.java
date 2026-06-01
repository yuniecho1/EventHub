package au.edu.rmit.sept.webapp.Feedback;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import au.edu.rmit.sept.webapp.model.Feedback;
import au.edu.rmit.sept.webapp.repository.FeedbackRepository;
import au.edu.rmit.sept.webapp.service.FeedbackService;

/**
 * @brief Unit test class for {@link FeedbackService}.
 *        Verifies business logic and validation rules for feedback management.
 * 
 * Ensures:
 * - Feedback validation correctly enforces business rules.
 * - Duplicate feedback submissions are properly prevented.
 * - Feedback creation and retrieval operations work as expected.
 */
@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private Feedback validFeedback;

    @BeforeEach
    public void setUp() {
        validFeedback = new Feedback();
        validFeedback.setEventId(1L);
        validFeedback.setUserEmail("test@example.com");
        validFeedback.setFeedbackText("Great event!");
        validFeedback.setRating(5);
    }

    /**
     * @brief Tests creating feedback with valid data.
     *
     * The test:
     * - Mocks repository to allow creation
     * - Calls createFeedback
     * - Verifies the feedback is saved and returned
     */
    @Test
    void shouldCreateFeedback_whenValidFeedback() {
        when(feedbackRepository.existsByEventIdAndUserEmail(1L, "test@example.com")).thenReturn(false);
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(validFeedback);

        Feedback result = feedbackService.createFeedback(validFeedback);

        assertNotNull(result);
        assertEquals("Great event!", result.getFeedbackText());
        verify(feedbackRepository).save(validFeedback);
    }

    /**
     * @brief Tests that duplicate feedback submission is prevented.
     *
     * The test:
     * - Mocks repository to indicate feedback already exists
     * - Calls createFeedback
     * - Verifies that an exception is thrown and save is not called
     */
    @Test
    void shouldThrowException_whenDuplicateFeedbackSubmitted() {
        when(feedbackRepository.existsByEventIdAndUserEmail(1L, "test@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> feedbackService.createFeedback(validFeedback)
        );
        assertEquals("You have already submitted feedback for this event", exception.getMessage());
        verify(feedbackRepository, never()).save(any());
    }

    /**
     * @brief Tests validating feedback with null event ID.
     *
     * The test:
     * - Sets event ID to null
     * - Calls validateFeedback
     * - Verifies that an exception is thrown
     */
    @Test
    void shouldThrowException_whenEventIdIsNull() {
        validFeedback.setEventId(null);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> feedbackService.validateFeedback(validFeedback)
        );
        assertEquals("Event ID cannot be null", exception.getMessage());
    }

    /**
     * @brief Tests validating feedback with empty feedback text.
     *
     * The test:
     * - Sets feedback text to empty
     * - Calls validateFeedback
     * - Verifies that an exception is thrown
     */
    @Test
    void shouldThrowException_whenFeedbackTextIsEmpty() {
        validFeedback.setFeedbackText("");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> feedbackService.validateFeedback(validFeedback)
        );
        assertEquals("Feedback text cannot be null or empty", exception.getMessage());
    }

    /**
     * @brief Tests validating feedback with overly long feedback text.
     *
     * The test:
     * - Sets feedback text length > 200 characters
     * - Calls validateFeedback
     * - Verifies that an exception is thrown
     */
    @Test
    void shouldThrowException_whenFeedbackTextTooLong() {
        validFeedback.setFeedbackText("a".repeat(201));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> feedbackService.validateFeedback(validFeedback)
        );
        assertEquals("Feedback text cannot exceed 200 characters", exception.getMessage());
    }

    /**
     * @brief Tests validating feedback with invalid rating.
     *
     * The test:
     * - Sets rating outside 1-5 range
     * - Calls validateFeedback
     * - Verifies that an exception is thrown
     */
    @Test
    void shouldThrowException_whenRatingOutOfRange() {
        validFeedback.setRating(6);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> feedbackService.validateFeedback(validFeedback)
        );
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }   

    /**
     * @brief Tests retrieving feedback for a specific event.
     *
     * The test:
     * - Mocks repository to return a list of feedbacks
     * - Calls getFeedbackForEvent
     * - Verifies the correct list is returned
     */
    @Test
    void shouldReturnFeedbackList_whenGettingFeedbackForEvent() {
        List<Feedback> feedbacks = Arrays.asList(validFeedback);
        when(feedbackRepository.findByEventId(1L)).thenReturn(feedbacks);

        List<Feedback> result = feedbackService.getFeedbackForEvent(1L);

        assertEquals(1, result.size());
        assertEquals("Great event!", result.get(0).getFeedbackText());
    }

    /**
     * @brief Tests checking if a user has submitted feedback for an event (true case).
     *
     * The test:
     * - Mocks repository to indicate feedback exists
     * - Calls hasUserSubmittedFeedback
     * - Verifies true is returned
     */
    @Test
    void shouldReturnTrue_whenUserHasSubmittedFeedback() {
        when(feedbackRepository.existsByEventIdAndUserEmail(1L, "test@example.com")).thenReturn(true);

        boolean result = feedbackService.hasUserSubmittedFeedback(1L, "test@example.com");

        assertTrue(result);
    }

    /**
     * @brief Tests checking if a user has submitted feedback for an event (false case).
     *
     * The test:
     * - Mocks repository to indicate feedback does not exist
     * - Calls hasUserSubmittedFeedback
     * - Verifies false is returned
     */
    @Test
    void shouldReturnFalse_whenUserHasNotSubmittedFeedback() {
        when(feedbackRepository.existsByEventIdAndUserEmail(1L, "test@example.com")).thenReturn(false);

        boolean result = feedbackService.hasUserSubmittedFeedback(1L, "test@example.com");

        assertFalse(result);
    }
}