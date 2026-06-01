package au.edu.rmit.sept.webapp.Feedback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import au.edu.rmit.sept.webapp.controller.EventController;
import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Feedback;
import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.ClubRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.FeedbackRepository;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @brief Integration test class for feedback-related functionality in {@link EventController}.
 *        Verifies controller logic for feedback submission and display controls.
 * 
 * Ensures:
 * - Feedback submission is restricted to authenticated students.
 * - Feedback options are shown based on role, RSVP status, and event timing.
 * - Controller handles validation errors and redirects properly.
 */
@SpringBootTest
@Transactional
class EventControllerFeedbackTest {

    @Autowired
    private EventController eventController;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RSVPRepository rsvpRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    private User studentUser;
    private User adminUser;
    private User organiserUser;
    private Event pastEvent;
    private Event futureEvent;
    private Club testClub;
    private Model model;
    private HttpServletRequest request;
    private RedirectAttributes redirectAttributes; 

    @BeforeEach
    public void setUp() {
        feedbackRepository.deleteAll();
        rsvpRepository.deleteAll();
        eventRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();

        testClub = new Club();
        testClub.setClubName("Test Club");
        testClub.setClubDescription("Test Description");
        testClub.setClubTag("test");
        testClub.setUserEmail("organiser@example.com");  
        testClub = clubRepository.save(testClub);

        studentUser = new User();
        studentUser.setUserEmail("student@example.com");
        studentUser.setUserName("Test Student");
        studentUser.setUserType("Student"); 
        studentUser.setUserPassword("password");
        studentUser = userRepository.save(studentUser);

        adminUser = new User();
        adminUser.setUserEmail("admin@example.com");
        adminUser.setUserName("Test Admin");
        adminUser.setUserType("Administrator");
        adminUser.setUserPassword("password");
        adminUser = userRepository.save(adminUser);

        organiserUser = new User();
        organiserUser.setUserEmail("organiser@example.com");    
        organiserUser.setUserName("Test Organiser");
        organiserUser.setUserType("Club_Organiser");
        organiserUser.setUserPassword("password");
        organiserUser = userRepository.save(organiserUser);

        pastEvent = new Event();
        pastEvent.setEventTitle("Past Event");
        pastEvent.setEventDate(LocalDate.now().minusDays(1));
        pastEvent.setClub(testClub);
        pastEvent.setEventDes("Past event description");
        pastEvent.setLocation("Location");
        pastEvent.setPrice("Free");
        pastEvent.setEventTag("test");
        pastEvent = eventRepository.save(pastEvent);

        futureEvent = new Event();
        futureEvent.setEventTitle("Future Event");
        futureEvent.setEventDate(LocalDate.now().plusDays(1));
        futureEvent.setClub(testClub);
        futureEvent.setEventDes("Future event description");
        futureEvent.setLocation("Location");
        futureEvent.setPrice("Free");
        futureEvent.setEventTag("test");
        futureEvent = eventRepository.save(futureEvent);

        model = new ExtendedModelMap();
        request = new MockHttpServletRequest();
        redirectAttributes = new RedirectAttributesModelMap(); 
    }

    /**
     * @brief Tests feedback option is shown for student with RSVP on a past event.
     *
     * The test:
     * - Adds RSVP for student on past event
     * - Sets student as authenticated
     * - Verifies feedback option is visible and hasSubmittedFeedback is false
     */
    @Test
    void getEventById_shouldShowFeedbackOption_forStudentWithRSVPAndPastEvent() {
        RSVP rsvp = new RSVP();
        rsvp.setUserEmail(studentUser.getUserEmail());
        rsvp.setEventId(pastEvent.getEventId());
        rsvp.setRsvpTimestamp(LocalDateTime.now());
        rsvpRepository.save(rsvp);

        request.setAttribute("authenticatedUser", studentUser);

        String result = eventController.getEventById(pastEvent.getEventId(), model, request);

        assertEquals("event-details", result);
        assertEquals(true, model.getAttribute("showFeedback"));
        assertEquals(false, model.getAttribute("hasSubmittedFeedback"));
    }

    /**
     * @brief Tests feedback option is not shown for student without RSVP.
     */
    @Test
    void getEventById_shouldNotShowFeedback_forStudentWithoutRSVP() {
        request.setAttribute("authenticatedUser", studentUser);

        String result = eventController.getEventById(pastEvent.getEventId(), model, request);

        assertEquals("event-details", result);
        assertEquals(false, model.getAttribute("showFeedback"));
    }

    /**
     * @brief Tests feedback option is not shown for student on a future event.
     */
    @Test
    void getEventById_shouldNotShowFeedback_forFutureEvent() {
        RSVP rsvp = new RSVP();
        rsvp.setUserEmail(studentUser.getUserEmail());
        rsvp.setEventId(futureEvent.getEventId());
        rsvp.setRsvpTimestamp(LocalDateTime.now());
        rsvpRepository.save(rsvp);

        request.setAttribute("authenticatedUser", studentUser);

        String result = eventController.getEventById(futureEvent.getEventId(), model, request);

        assertEquals("event-details", result);
        assertEquals(false, model.getAttribute("showFeedback"));
    }

    /**
     * @brief Tests existing feedback is displayed for student who already submitted feedback.
     */
    @Test
    void getEventById_shouldShowExistingFeedback_forStudentWhoSubmitted() {
        RSVP rsvp = new RSVP();
        rsvp.setUserEmail(studentUser.getUserEmail());
        rsvp.setEventId(pastEvent.getEventId());
        rsvp.setRsvpTimestamp(LocalDateTime.now());
        rsvpRepository.save(rsvp);

        Feedback existingFeedback = new Feedback();
        existingFeedback.setEventId(pastEvent.getEventId());
        existingFeedback.setUserEmail(studentUser.getUserEmail());
        existingFeedback.setFeedbackText("Previous feedback");
        existingFeedback.setRating(4);
        existingFeedback.setSubmissionDate(LocalDateTime.now());
        feedbackRepository.save(existingFeedback);

        request.setAttribute("authenticatedUser", studentUser);

        String result = eventController.getEventById(pastEvent.getEventId(), model, request);

        assertEquals("event-details", result);
        assertEquals(true, model.getAttribute("hasSubmittedFeedback"));
        assertNotNull(model.getAttribute("userFeedback"));
    }

    /**
     * @brief Tests a student can successfully submit feedback for a past event.
     */
    @Test
    void submitFeedback_shouldSaveFeedback_forValidStudent() {
        RSVP rsvp = new RSVP();
        rsvp.setUserEmail(studentUser.getUserEmail());
        rsvp.setEventId(pastEvent.getEventId());
        rsvp.setRsvpTimestamp(LocalDateTime.now());
        rsvpRepository.save(rsvp);

        request.setAttribute("authenticatedUser", studentUser);

        String result = eventController.submitFeedback(pastEvent.getEventId(), "Great event!", 5, request, model, redirectAttributes);

        assertEquals("redirect:/event/" + pastEvent.getEventId() + "?feedback=success", result);
        assertEquals(1, feedbackRepository.count());
        Feedback saved = feedbackRepository.findAll().get(0);
        assertEquals("Great event!", saved.getFeedbackText());
        assertEquals(5, saved.getRating());
    }

    /**
     * @brief Tests feedback submission by non-student redirects to sign-in with error.
     */
    @Test
    void submitFeedback_shouldRedirectToSignIn_forNonStudent() {
        request.setAttribute("authenticatedUser", adminUser);

        String result = eventController.submitFeedback(pastEvent.getEventId(), "Great event!", 5, request, model, redirectAttributes);

        assertEquals("redirect:/sign-in", result);
        assertEquals("You must be a registered student to submit feedback.", redirectAttributes.getFlashAttributes().get("error"));
        assertEquals(0, feedbackRepository.count());
    }

    /**
     * @brief Tests submitting duplicate feedback by the same student is blocked.
     */
    @Test
    void submitFeedback_shouldPreventDuplicateSubmission_forSameStudent() {
        RSVP rsvp = new RSVP();
        rsvp.setUserEmail(studentUser.getUserEmail());
        rsvp.setEventId(pastEvent.getEventId());
        rsvp.setRsvpTimestamp(LocalDateTime.now());
        rsvpRepository.save(rsvp);

        request.setAttribute("authenticatedUser", studentUser);

        // Submit first feedback
        Model model1 = new ExtendedModelMap();
        RedirectAttributes redirectAttributes1 = new RedirectAttributesModelMap();
        String firstResult = eventController.submitFeedback(pastEvent.getEventId(), "Great event!", 5, request, model1, redirectAttributes1);
        assertEquals("redirect:/event/" + pastEvent.getEventId() + "?feedback=success", firstResult);

        // Try duplicate
        Model model2 = new ExtendedModelMap();
        RedirectAttributes redirectAttributes2 = new RedirectAttributesModelMap();
        String result = eventController.submitFeedback(pastEvent.getEventId(), "Another feedback", 4, request, model2, redirectAttributes2);

        assertTrue(result.startsWith("redirect:/event/" + pastEvent.getEventId() + "?feedback=error"));
        assertEquals("You have already submitted feedback for this event.", redirectAttributes2.getFlashAttributes().get("error"));
        assertEquals(1, feedbackRepository.count());
    }

    /**
     * @brief Tests that leading/trailing whitespace is trimmed when saving feedback.
     */
    @Test
    void submitFeedback_shouldTrimWhitespace_inFeedbackText() {
        RSVP rsvp = new RSVP();
        rsvp.setUserEmail(studentUser.getUserEmail());
        rsvp.setEventId(pastEvent.getEventId());
        rsvp.setRsvpTimestamp(LocalDateTime.now());
        rsvpRepository.save(rsvp);

        request.setAttribute("authenticatedUser", studentUser);

        String result = eventController.submitFeedback(pastEvent.getEventId(), "  Good event  ", 4, request, model, redirectAttributes);

        assertEquals("redirect:/event/" + pastEvent.getEventId() + "?feedback=success", result);
        assertEquals(1, feedbackRepository.count());
        Feedback savedFeedback = feedbackRepository.findAll().get(0);
        assertEquals("Good event", savedFeedback.getFeedbackText());
        assertEquals(4, savedFeedback.getRating());
        assertEquals(pastEvent.getEventId(), savedFeedback.getEventId());
        assertEquals(studentUser.getUserEmail(), savedFeedback.getUserEmail());
    }

    /**
     * @brief Tests organiser can view all feedback for an event.
     */
    @Test
    void getEventById_shouldShowAllFeedback_forOrganiser() {
        createMultipleFeedback();

        request.setAttribute("authenticatedUser", organiserUser);

        String result = eventController.getEventById(pastEvent.getEventId(), model, request);

        assertEquals("event-details", result);
        assertEquals(true, model.getAttribute("showAllFeedback"));

        @SuppressWarnings("unchecked")
        List<Feedback> allFeedback = (List<Feedback>) model.getAttribute("allFeedback");
        assertNotNull(allFeedback);
        assertEquals(3, allFeedback.size());
    }

    /**
     * @brief Tests admin can view all feedback for an event.
     */
    @Test
    void getEventById_shouldShowAllFeedback_forAdmin() {
        createMultipleFeedback();

        request.setAttribute("authenticatedUser", adminUser);

        String result = eventController.getEventById(pastEvent.getEventId(), model, request);

        assertEquals("event-details", result);
        assertEquals(true, model.getAttribute("showAllFeedback"));

        @SuppressWarnings("unchecked")
        List<Feedback> allFeedback = (List<Feedback>) model.getAttribute("allFeedback");
        assertNotNull(allFeedback);
        assertEquals(3, allFeedback.size());
    }

    /**
     * @brief Tests organiser sees empty feedback list if no feedback exists.
     */
    @Test
    void getEventById_shouldShowEmptyFeedbackList_whenNoFeedbackExists() {
        request.setAttribute("authenticatedUser", organiserUser);

        String result = eventController.getEventById(pastEvent.getEventId(), model, request);

        assertEquals("event-details", result);
        assertEquals(true, model.getAttribute("showAllFeedback"));

        @SuppressWarnings("unchecked")
        List<Feedback> allFeedback = (List<Feedback>) model.getAttribute("allFeedback");
        assertNotNull(allFeedback);
        assertTrue(allFeedback.isEmpty());
    }

    /**
     * @brief Tests organiser does not see student feedback or RSVP options.
     */
    @Test
    void getEventById_shouldHideStudentFeedbackAndRSVP_forOrganiser() {
        request.setAttribute("authenticatedUser", organiserUser);

        String result = eventController.getEventById(pastEvent.getEventId(), model, request);

        assertEquals("event-details", result);
        assertEquals(false, model.getAttribute("showFeedback"));  
        assertEquals(false, model.getAttribute("showRSVP"));     
    }

    /**
     * @brief Tests organiser sees edit, delete, and add picture buttons.
     */
    @Test
    void getEventById_shouldShowEditDeleteAddPicture_forOrganiser() {
        request.setAttribute("authenticatedUser", organiserUser);

        String result = eventController.getEventById(pastEvent.getEventId(), model, request);

        assertEquals("event-details", result);
        assertEquals(true, model.getAttribute("showEdit"));
        assertEquals(true, model.getAttribute("showDelete"));
        assertEquals(true, model.getAttribute("showAddPicture"));
    }

    /**
     * @brief Helper method to create multiple feedback entries for testing.
     */
    private void createMultipleFeedback() {
        User student1 = new User();
        student1.setUserEmail("student1@example.com");
        student1.setUserName("Student One");
        student1.setUserType("Student");
        student1.setUserPassword("password");
        userRepository.save(student1);

        User student2 = new User();
        student2.setUserEmail("student2@example.com");
        student2.setUserName("Student Two");
        student2.setUserType("Student");
        student2.setUserPassword("password");
        userRepository.save(student2);

        User student3 = new User();
        student3.setUserEmail("student3@example.com");
        student3.setUserName("Student Three");
        student3.setUserType("Student");
        student3.setUserPassword("password");
        userRepository.save(student3);

        RSVP rsvp1 = new RSVP();
        rsvp1.setUserEmail(student1.getUserEmail());
        rsvp1.setEventId(pastEvent.getEventId());
        rsvp1.setRsvpTimestamp(LocalDateTime.now().minusDays(5));
        rsvpRepository.save(rsvp1);

        RSVP rsvp2 = new RSVP();
        rsvp2.setUserEmail(student2.getUserEmail());
        rsvp2.setEventId(pastEvent.getEventId());
        rsvp2.setRsvpTimestamp(LocalDateTime.now().minusDays(4));
        rsvpRepository.save(rsvp2);

        RSVP rsvp3 = new RSVP();
        rsvp3.setUserEmail(student3.getUserEmail());
        rsvp3.setEventId(pastEvent.getEventId());
        rsvp3.setRsvpTimestamp(LocalDateTime.now().minusDays(3));
        rsvpRepository.save(rsvp3);

        Feedback feedback1 = new Feedback();
        feedback1.setEventId(pastEvent.getEventId());
        feedback1.setUserEmail(student1.getUserEmail());
        feedback1.setFeedbackText("Great event! Learned a lot.");
        feedback1.setRating(5);
        feedback1.setSubmissionDate(LocalDateTime.now().minusDays(1));
        feedbackRepository.save(feedback1);

        Feedback feedback2 = new Feedback();
        feedback2.setEventId(pastEvent.getEventId());
        feedback2.setUserEmail(student2.getUserEmail());
        feedback2.setFeedbackText("Good workshop, but could be longer.");
        feedback2.setRating(4);
        feedback2.setSubmissionDate(LocalDateTime.now().minusDays(2));
        feedbackRepository.save(feedback2);

        Feedback feedback3 = new Feedback();
        feedback3.setEventId(pastEvent.getEventId());
        feedback3.setUserEmail(student3.getUserEmail());
        feedback3.setFeedbackText("Could be better.");
        feedback3.setRating(3);
        feedback3.setSubmissionDate(LocalDateTime.now().minusDays(3));
        feedbackRepository.save(feedback3);
    }
}