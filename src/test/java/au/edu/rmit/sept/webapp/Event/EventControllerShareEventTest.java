package au.edu.rmit.sept.webapp.Event;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.time.LocalDate;
import java.util.Base64;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.ClubService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.FeedbackService;
import au.edu.rmit.sept.webapp.service.RSVPService;
import au.edu.rmit.sept.webapp.service.UserService;

/**
 * @brief Integration test for share event functionality on event details page.
 * 
 * @author [Yunie Cho]
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerShareEventTest {

    private static final String EVENT_DETAILS_URL = "/event/1";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private ClubService clubService;

    @MockBean
    private FeedbackService feedbackService;

    @MockBean
    private RSVPService rsvpService;

    @MockBean
    private UserService userService;  

    private Event event;
    private Club club;
    private User studentUser;
    private User organiserUser;

    @BeforeEach
    public void setUp() {
        // Setup club
        club = new Club();
        club.setClubId(1L);
        club.setClubName("Test Club");
        club.setUserEmail("organiser@example.com");

        // Setup event
        event = new Event();
        event.setEventId(1L);
        event.setEventTitle("Future Event");
        event.setEventDate(LocalDate.now().plusDays(7));
        event.setEventDes("An upcoming event");
        event.setLocation("10.08.21");
        event.setPrice("$10");
        event.setEventTag("tech");
        event.setClub(club);

        // Setup student user
        studentUser = new User();
        studentUser.setUserEmail("student@example.com");
        studentUser.setUserName("Test Student");
        studentUser.setUserType("Student");
        studentUser.setUserPassword("password123");

        // Setup organiser user
        organiserUser = new User();
        organiserUser.setUserEmail("organiser@example.com");
        organiserUser.setUserName("Test Organiser");
        organiserUser.setUserType("Club_Organiser");
        organiserUser.setUserPassword("password123");
    }

    // ===== HELPER METHODS ===== //

    /**
     * Creates an auth token cookie for the given user
     */
    private Cookie createAuthTokenCookie(User user, String password) {
        String token = createAuthToken(user.getUserEmail(), password);
        return new Cookie("authToken", token);
    }

    /**
     * Creates a Base64-encoded auth token (mimics UserService.createAuthToken)
     */
    private String createAuthToken(String email, String password) {
        String tokenStr = email + ":" + password;
        return Base64.getEncoder().encodeToString(tokenStr.getBytes());
    }

    /**
     * Mocks UserService to verify the auth token and return the user
     */
    private void mockUserServiceAuth(User user, String password) {
        String token = createAuthToken(user.getUserEmail(), password);
        when(userService.verifyLogin(token)).thenReturn(true);
        when(userService.getUserByAuthToken(token)).thenReturn(user);
    }

    // ===== TESTS ===== //

    /**
     * Test 1: Verify that a student user can see the Share button on event details page.
     */
    @Test
    void shouldDisplayShareButtonForStudent() throws Exception {
        when(eventService.getEventById(1L)).thenReturn(event);
        when(clubService.getClubById(1L)).thenReturn(club);
        when(rsvpService.hasUserRSVPd("student@example.com", 1L)).thenReturn(false);
        
        String password = "password123";
        mockUserServiceAuth(studentUser, password);
        Cookie authCookie = createAuthTokenCookie(studentUser, password);

        mvc.perform(get(EVENT_DETAILS_URL).cookie(authCookie))
                .andExpect(status().isOk())                           
                .andExpect(content().string(containsString("Share"))) 
                .andExpect(model().attribute("showShare", true));     
    }

    /**
     * Test 2: Verify that a club organiser can see the Share button.
     */
    @Test
    void shouldDisplayShareButtonForOrganiser() throws Exception {
        when(eventService.getEventById(1L)).thenReturn(event);
        when(clubService.getClubById(1L)).thenReturn(club);
        
        String password = "password123";
        mockUserServiceAuth(organiserUser, password);
        Cookie authCookie = createAuthTokenCookie(organiserUser, password);

        mvc.perform(get(EVENT_DETAILS_URL).cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Share")))
                .andExpect(model().attribute("showShare", true));
    }

    /**
     * Test 3: Verify that the Share button includes JavaScript functionality.
     */
    @Test
    void shouldDisplayShareButtonWithJavaScript() throws Exception {
        when(eventService.getEventById(1L)).thenReturn(event);
        when(clubService.getClubById(1L)).thenReturn(club);
        when(rsvpService.hasUserRSVPd("student@example.com", 1L)).thenReturn(false);
        
        String password = "password123";
        mockUserServiceAuth(studentUser, password);
        Cookie authCookie = createAuthTokenCookie(studentUser, password);

        mvc.perform(get(EVENT_DETAILS_URL).cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("copyEventLink()"))) 
                .andExpect(content().string(containsString("shareButton")));    
    }

    /**
     * Test 4: Verify that isAuthenticated is set to true for authenticated users.
     */
    @Test
    void shouldSetIsAuthenticatedTrue() throws Exception {
        when(eventService.getEventById(1L)).thenReturn(event);
        when(clubService.getClubById(1L)).thenReturn(club);
        when(rsvpService.hasUserRSVPd("student@example.com", 1L)).thenReturn(false);
        
        String password = "password123";
        mockUserServiceAuth(studentUser, password);
        Cookie authCookie = createAuthTokenCookie(studentUser, password);

        mvc.perform(get(EVENT_DETAILS_URL).cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isAuthenticated", true));
    }

    /**
     * Test 5: Verify that unauthenticated users are redirected to sign-in.
     */
    @Test
    void shouldSetIsAuthenticatedFalseForUnauthenticatedUser() throws Exception {
        when(eventService.getEventById(1L)).thenReturn(event);
        when(clubService.getClubById(1L)).thenReturn(club);

        mvc.perform(get(EVENT_DETAILS_URL))
                .andExpect(status().is3xxRedirection())  
                .andExpect(status().is(302));             
    }


    /**
     * Test 6: Verify that students see both RSVP and Share buttons simultaneously.
     */
    @Test
    void shouldDisplayRSVPAndShareButtonsTogether() throws Exception {
        // Arrange: Set up student who hasn't RSVP'd yet
        when(eventService.getEventById(1L)).thenReturn(event);
        when(clubService.getClubById(1L)).thenReturn(club);
        when(rsvpService.hasUserRSVPd("student@example.com", 1L)).thenReturn(false);
        
        String password = "password123";
        mockUserServiceAuth(studentUser, password);
        Cookie authCookie = createAuthTokenCookie(studentUser, password);

        // Act & Assert: Verify both buttons exist in HTML and model
        mvc.perform(get(EVENT_DETAILS_URL).cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("RSVP")))   
                .andExpect(content().string(containsString("Share")))  
                .andExpect(model().attribute("showRSVP", true))        
                .andExpect(model().attribute("showShare", true));     
    }

    /**
     * Test 9: Verify that the success feedback message exists in the HTML.
     */
    @Test
    void shouldDisplayLinkCopiedFeedback() throws Exception {
        when(eventService.getEventById(1L)).thenReturn(event);
        when(clubService.getClubById(1L)).thenReturn(club);
        when(rsvpService.hasUserRSVPd("student@example.com", 1L)).thenReturn(false);
        
        String password = "password123";
        mockUserServiceAuth(studentUser, password);
        Cookie authCookie = createAuthTokenCookie(studentUser, password);

        mvc.perform(get(EVENT_DETAILS_URL).cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Link Copied!")));
    }
}