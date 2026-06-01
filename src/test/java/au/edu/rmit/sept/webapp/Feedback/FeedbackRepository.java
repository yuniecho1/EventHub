package au.edu.rmit.sept.webapp.Feedback;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import au.edu.rmit.sept.webapp.model.Feedback;
import au.edu.rmit.sept.webapp.repository.FeedbackRepository;

/**
 * @brief Unit test class for {@link FeedbackRepository}.
 *        Verifies CRUD operations and query methods.
 * 
 * Ensures:
 * - Feedback entities are correctly saved and retrieved.
 * - Query methods return accurate results for event and user filters.
 * - Duplicate feedback checks work at the repository level.
 */
@DataJpaTest
class FeedbackRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FeedbackRepository feedbackRepository;

    private Feedback feedback1;
    private Feedback feedback2;

    @BeforeEach
    public void setUp() {
        feedback1 = new Feedback();
        feedback1.setEventId(1L);
        feedback1.setUserEmail("user1@example.com");
        feedback1.setFeedbackText("Great event!");
        feedback1.setRating(5);

        feedback2 = new Feedback();
        feedback2.setEventId(1L);
        feedback2.setUserEmail("user2@example.com");
        feedback2.setFeedbackText("Good experience");
        feedback2.setRating(4);
    }

    /**
     * @brief Tests saving a feedback entity.
     *
     * The test:
     * - Saves a feedback object
     * - Verifies that the ID is generated and fields are stored correctly
     */
    @Test
    void saveFeedback_shouldPersistFeedbackCorrectly() {
        Feedback saved = feedbackRepository.save(feedback1);
        
        assertThat(saved.getFeedbackId()).isNotNull();
        assertThat(saved.getFeedbackText()).isEqualTo("Great event!");
        assertThat(saved.getRating()).isEqualTo(5);
    }

    /**
     * @brief Tests finding feedback by event ID.
     *
     * The test:
     * - Persists multiple feedbacks for the same event
     * - Retrieves them by event ID
     * - Verifies the correct number and emails of users
     */
    @Test
    void findByEventId_shouldReturnAllFeedback_forGivenEvent() {
        entityManager.persistAndFlush(feedback1);
        entityManager.persistAndFlush(feedback2);

        List<Feedback> results = feedbackRepository.findByEventId(1L);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Feedback::getUserEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    /**
     * @brief Tests finding feedback by user email.
     *
     * The test:
     * - Persists feedback for multiple events by same user
     * - Retrieves them by user email
     * - Verifies all relevant event IDs are returned
     */
    @Test
    void findByUserEmail_shouldReturnAllFeedback_forGivenUser() {
        Feedback feedback3 = new Feedback();
        feedback3.setEventId(2L);
        feedback3.setUserEmail("user1@example.com");
        feedback3.setFeedbackText("Another event");
        feedback3.setRating(3);

        entityManager.persistAndFlush(feedback1);
        entityManager.persistAndFlush(feedback3);

        List<Feedback> results = feedbackRepository.findByUserEmail("user1@example.com");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Feedback::getEventId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    /**
     * @brief Tests existence check by event ID and user email.
     *
     * The test:
     * - Persists a feedback
     * - Checks that existsByEventIdAndUserEmail returns true for that entry
     * - Checks false for a non-existent entry
     */
    @Test
    void existsByEventIdAndUserEmail_shouldReturnTrueForExistingFeedback() {
        entityManager.persistAndFlush(feedback1);

        boolean exists = feedbackRepository.existsByEventIdAndUserEmail(1L, "user1@example.com");
        boolean notExists = feedbackRepository.existsByEventIdAndUserEmail(1L, "nonexistent@example.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    /**
     * @brief Tests finding feedback by event ID and user email.
     *
     * The test:
     * - Persists multiple feedback entries
     * - Retrieves a specific user's feedback for an event
     * - Verifies the retrieved feedback text
     */
    @Test
    void findByEventIdAndUserEmail_shouldReturnFeedback_forSpecificUserAndEvent() {
        entityManager.persistAndFlush(feedback1);
        entityManager.persistAndFlush(feedback2);

        List<Feedback> results = feedbackRepository.findByEventIdAndUserEmail(1L, "user1@example.com");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFeedbackText()).isEqualTo("Great event!");
    }

    /**
     * @brief Tests finding feedback by event ID when no results exist.
     *
     * The test:
     * - Attempts to find feedback for a non-existent event
     * - Verifies the result is empty
     */
    @Test
    void findByEventId_shouldReturnEmptyList_whenNoFeedbackExists() {
        List<Feedback> results = feedbackRepository.findByEventId(999L);
        
        assertThat(results).isEmpty();
    }
}