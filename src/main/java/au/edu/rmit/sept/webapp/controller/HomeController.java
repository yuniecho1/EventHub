package au.edu.rmit.sept.webapp.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;
import au.edu.rmit.sept.webapp.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {
    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private RSVPService rsvpService;

    /*
     * Landing page
     */
    @GetMapping("/")
    public String landingPage() {
        return "landing-page";
    }

    /**
     * Displays the sign-up page.
     * On accessing the sign-up page, checks if an auth token cookie exists and is valid.
     * If valid, redirects to the dashboard. Otherwise, shows the sign-up page.
     * @param model the model
     * @param request the HTTP request
     * @return the sign-up view name
     */
    @GetMapping("/sign-up")
    public String signUpPage(Model model, HttpServletRequest request) {
        model.addAttribute("user", new User());
        String authToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    authToken = cookie.getValue();
                    break;
                }
            }
        }
        if (authToken != null && userService.verifyLogin(authToken)) {
            return "redirect:/dashboard";
        }
        return "sign-up";
    }

    /**
     * Handles user registration.
     * On form submission, attempts to register the user. If successful, redirects to sign-in with a success message.
     * If registration fails (e.g., validation errors), returns to sign-up with error messages.
     * @param user the user model attribute
     * @param model the model
     * @return the next view name
     */
    @PostMapping("/sign-up")
    public String handleSignUp(@ModelAttribute User user, Model model) {
        
        if (user.getUserEmail().equalsIgnoreCase("super@admin.com")) {
            model.addAttribute("error", "Registration with this email is not allowed.");
            model.addAttribute("user", user);
            return "sign-up";
        }

        try {
            userService.registerUser(
                user.getFirstName(),
                user.getSurname(),
                user.getEmail(),
                user.getPassword(),
                user.getConfirmPassword(),
                user.getRole()
            );
            model.addAttribute("successMessage", "Registration successful! Please sign in.");
            model.addAttribute("user", new User());
            return "sign-in";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "sign-up";
        }
        catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("user", user);
            return "sign-up";
        }
    }

    /**
     * Displays the sign-in page.
     * On accessing the sign-in page, checks if an auth token cookie exists and is valid.
     * If valid, redirects to the dashboard. Otherwise, shows the sign-in page.
     * @param model the model
     * @param request the HTTP request
     * @return the sign-in view name
     */
    @GetMapping("/sign-in")
    public String signInPage(Model model, HttpServletRequest request) {
        model.addAttribute("user", new User());
        String authToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    authToken = cookie.getValue();
                    break;
                }
            }
        }
        if (authToken != null && userService.verifyLogin(authToken)) {
            return "redirect:/dashboard";
        }
        return "sign-in";
    }
 
    /**
     * Handles user authentication.
     * On form submission, attempts to authenticate the user. If successful, redirects to the dashboard with an auth token cookie.
     * If authentication fails, returns to sign-in with error messages.
     * @param user the user model attribute
     * @param model the model
     * @param response the HTTP response to set cookies
     * @return the next view name
     */
    @PostMapping("/sign-in")
    public String handleSignIn(@ModelAttribute User user, Model model, HttpServletResponse response) {
        
        if(user.getUserEmail().equalsIgnoreCase("super@admin.com") && user.getPassword().equals("SuperAdmin123")) {
            if(userService.getUserByEmail("super@admin.com") == null) {
                try {
                    userService.registerUser(
                        "Super",
                        "Admin",
                        "super@admin.com",
                        "SuperAdmin123", 
                        "SuperAdmin123",
                        "Administrator"
                    );
                }
                catch (Exception ex) {
                    model.addAttribute("error", "Failed to create super admin: " + ex.getMessage());
                    model.addAttribute("user", user);
                    return "sign-in";
                }
            }
        }

        try {
            User authenticatedUser = userService.authenticateUser(
                user.getEmail(),
                user.getPassword()
            );
            model.addAttribute("user", authenticatedUser);
            String authToken = userService.createAuthToken(
                authenticatedUser.getEmail(),
                user.getPassword()
            );
            model.addAttribute("authToken", authToken);
            response.addCookie(createCookie("authToken", authToken));
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "sign-in";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("user", user);
            return "sign-in";
        }
    }
    /**
     * Displays the user dashboard.
     * Checks for a valid auth token cookie. If valid, retrieves user info and displays the dashboard.
     * If not valid, redirects to sign-in.
     * @param model the model
     * @param request the HTTP request
     * @return the dashboard view name or redirect to sign-in
     */
    @GetMapping("/dashboard")
    public String dashboardPage(Model model, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");

        model.addAttribute("user", user);
        model.addAttribute("userType", user.getUserType());

        if (user.getUserType().equalsIgnoreCase("student"))
        {
            try {
            List<RSVP> userRSVPs = rsvpService.getRSVPsForUser(user.getUserEmail());
            List<Event> rsvpEvents = new ArrayList<>();
            List<Event> pastEvents = new ArrayList<>();

            
            // Fetch events for each RSVP and filter out past events
            for (RSVP rsvp : userRSVPs) {
                Event event = eventService.getEventById(rsvp.getEventId());
                if (event != null) {
                    if (event.getEventDate().isBefore(LocalDate.now())) {
                        pastEvents.add(event);
                    } else if (event.getEventDate().isAfter(LocalDate.now().minusDays(1))) {
                        rsvpEvents.add(event);
                    }
                }
            }

            // Sort by date and limit to 5
            rsvpEvents.sort((e1, e2) -> e1.getEventDate().compareTo(e2.getEventDate()));
            pastEvents.sort((e1, e2) -> e2.getEventDate().compareTo(e1.getEventDate()));

            if (rsvpEvents.size() > 5) {
                rsvpEvents = rsvpEvents.subList(0, 5);
            }

            if (pastEvents.size() > 5) {
                pastEvents = pastEvents.subList(0, 5);
            }
            
            model.addAttribute("rsvpEvents", rsvpEvents);
            model.addAttribute("pastEvents", pastEvents);
        } catch (Exception e) {
            model.addAttribute("rsvpEvents", new ArrayList<>());
            model.addAttribute("pastEvents", new ArrayList<>());
            model.addAttribute("rsvpError", "Unable to load RSVP'd events");
        }

        // Fetch upcoming (5) events for the dashboard
        try {
            List<Event> upcomingEvents = eventService.getUpcomingEventsForDashboard(); 
            model.addAttribute("upcomingEvents", upcomingEvents);
        } catch (Exception e) {
            model.addAttribute("upcomingEvents", new ArrayList<>());
            model.addAttribute("eventError", "Unable to load upcoming events");
        }

            return "student-dashboard";
        }
        else if (user.getUserType().equalsIgnoreCase("club_organiser")) 
        {
            return "organiser-dashboard";
        }
      
        else if (user.getUserType().equalsIgnoreCase("administrator")) 
        {
            return "admin-dashboard";
        }
        return "error";
    }

    /**
     * Creates a cookie with the given name and value.
     * The cookie is set to HttpOnly and has a path of "/".
     * @param name the cookie name
     * @param value the cookie value
     * @return the created Cookie
     */
    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    /**
     * @brief Logs out the currently authenticated user by clearing the authentication cookie.
     * Iterates through all cookies in the request, removes the {@code authToken} cookie if present,
     * and then redirects the user back to the sign-in page.
     *
     * @param request  the current HTTP request, used to retrieve cookies
     * @param response the HTTP response, used to add the cleared cookie back to the client
     * @return a redirect string that sends the user to the sign-in page
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    //remove cookie
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }

        // Redirect back to login page
        return "redirect:/sign-in";
    }
}