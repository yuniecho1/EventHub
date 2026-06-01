package au.edu.rmit.sept.webapp.controller;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Feedback;
import au.edu.rmit.sept.webapp.model.Photo;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.ClubService;
import au.edu.rmit.sept.webapp.service.EmailService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.FeedbackService;
import au.edu.rmit.sept.webapp.service.PhotoService;
import au.edu.rmit.sept.webapp.service.RSVPService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @brief Controller responsible for handling HTTP requests related to {@link Event} entities.
 * Provides endpoints for displaying the event creation form and processing event submissions.
 * Handles the interaction between the view layer and the {@link EventService} for creating events.
 * 
 * @author Lucas Aponso
 * @version 1.0
 */
@Controller
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private RSVPService rsvpService;    

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private ClubService clubService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private org.thymeleaf.spring6.SpringTemplateEngine templateEngine;

    
    /**
     * @brief Displays the form for creating a new event.
     *
     * @param model the model to bind attributes to the view
     * @return the name of the Thymeleaf template for the event creation form
     */
    @GetMapping("/event/form")
    public String showEventForm(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        model.addAttribute("event", new Event());

        //populate the event details with club selections
        List<Club> clubList = clubService.getClubs();
        if (clubList == null || clubList.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No clubs available. Please create or join a club first.");
            return "redirect:/event/all";
        }

         User authenticatedUser = (User) request.getAttribute("authenticatedUser");
         model.addAttribute("userType", authenticatedUser.getUserType());
         
         // Only allow club organisers to create events for their own clubs
         if ("Club_Organiser".equals(authenticatedUser.getUserType())) {
             clubList = clubList.stream()
                     .filter(c -> c.getUserEmail().equals(authenticatedUser.getUserEmail()))
                     .collect(Collectors.toList());
         }

         if (clubList.isEmpty()) {
             model.addAttribute("error", "You must be an organiser of at least one club to create events.");
             return "redirect:/event/all";
         }

        model.addAttribute("clubList", clubList);
        return "event-form";
    }
    /**
     * @brief The following endpoint uploads a photo to an events page
     */
    @PostMapping("/event/{eventId}/upload")
    public String uploadEventPhoto(
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file, 
            RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:/event/" + eventId;
        }

        try {
            photoService.addPhotoToEvent(file, eventId);
            
            String fileName = file.getOriginalFilename();
            redirectAttributes.addFlashAttribute("success", 
                "File uploaded successfully: " + fileName);
                
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", 
                "Failed to upload file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/event/" + eventId;
    }

    /**
     * @brief Handles the submission of the event creation form.
     * Attempts to create a new event using the {@link EventService}. On success,
     * returns to the all events page. On failure,
     * redisplays the form with an error message.
     *
     * @param event the event object bound from the form submission
     * @param model the model to bind attributes to the view
     * @return the name of the view template to render
     */
    @PostMapping("/event/create")
    public String handleEventSubmission(@ModelAttribute Event event, Model model) {
        try {
            Event createdEvent = eventService.createEvent(event);
            model.addAttribute("event", createdEvent);
            return "redirect:/event/all";
        } catch (Exception e) {
            model.addAttribute("clubList", clubService.getClubs());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("event", event);
            return "event-form";
        }
    }

    /**
     * @brief Retrieves and displays a list of all events.
     * Supports optional search, tag filtering, club filtering, and sorting.
     *
     * @param query optional search string to filter events
     * @param sort optional sorting criteria ("title", "date", or "location")
     * @param filteredTag optional tag filter
     * @param filteredClub optional club filter
     * @param model the model to bind attributes to the view
     * @param request the HTTP request to get authenticated user info
     * @return the name of the event list template
     */
    @GetMapping("/event/all")
    public String handleEventRetrieval(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "filteredTag", required = false) String filteredTag,
            @RequestParam(value = "filteredClub", required = false) String filteredClub,
            Model model,
            HttpServletRequest request) {
        try {
            List<Event> eventList = eventService.searchEvents(query);

            //filter by tag
            if (filteredTag != null && !filteredTag.isEmpty()) {
                eventList = eventList.stream()
                        .filter(e -> e.getEventTag().equalsIgnoreCase(filteredTag))
                        .collect(Collectors.toList());
            }

            //filter by club
            if (filteredClub != null && !filteredClub.isEmpty()) {
                eventList = eventList.stream()
                        .filter(e -> e.getClub().getClubName().equalsIgnoreCase(filteredClub))
                        .collect(Collectors.toList());
            }

            //default sort by title
            eventList.sort(Comparator.comparing(Event::getEventTitle, String.CASE_INSENSITIVE_ORDER));

            if (sort != null && !sort.isEmpty()) {
                switch (sort) {
                    case "title" -> eventList.sort(Comparator.comparing(Event::getEventTitle, String.CASE_INSENSITIVE_ORDER));
                    case "date" -> eventList.sort(Comparator.comparing(Event::getEventDate));
                    case "location" -> eventList.sort(Comparator.comparing(Event::getLocation, String.CASE_INSENSITIVE_ORDER));
                    default -> {}
                }
            }

            model.addAttribute("eventList", eventList);
            model.addAttribute("query", query);
            model.addAttribute("sort", sort);
            List<String> allTags = eventService.findAllTags();
            model.addAttribute("allTags", allTags);
            model.addAttribute("filteredTag", filteredTag);

            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            model.addAttribute("userType", authenticatedUser.getUserType());
            
            List<String> allClubs = clubService.findAllClubNames();
            model.addAttribute("allClubs", allClubs);
            model.addAttribute("filteredClub", filteredClub);

            return "event-list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "event-list";
        }
    }

    /**
     * @brief Redirects to the details page for the selected event.
     *
     * @param eventId the ID of the clicked event
     * @return a redirect string to the event details page
     */
    @PostMapping("/event/all")
    public String handleEventClick(@RequestParam Long eventId) {
        return "redirect:/event/" + eventId;
    }

    /**
     * @brief Retrieves and displays the details of a specific event.
     * Dynamically determines which UI controls to show based on the
     * authenticated user's role and RSVP status.
     *
     * @param eventId the ID of the event to retrieve
     * @param model   the model to bind attributes to the view
     * @param request the HTTP request to obtain the authenticated user
     * @return the name of the event details template
     */
    @GetMapping("/event/{eventId}")
public String getEventById(@PathVariable Long eventId, Model model, HttpServletRequest request) {        
    try {
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        model.addAttribute("showRSVP", false);
        model.addAttribute("showShare", false);
        model.addAttribute("showEdit", false);
        model.addAttribute("showDelete", false);
        model.addAttribute("showAddPicture", false);
        model.addAttribute("userHasRSVPd", false);
        model.addAttribute("showFeedback", false);
        model.addAttribute("hasSubmittedFeedback", false); // Initialize to avoid null
        model.addAttribute("showAllFeedback", false);
        model.addAttribute("isAuthenticated", false);

        if (authenticatedUser != null) {
            model.addAttribute("user", authenticatedUser);
            model.addAttribute("userRole", authenticatedUser.getUserType());
            model.addAttribute("isAuthenticated", true);
        
            String userType = authenticatedUser.getUserType();
            if ("student".equalsIgnoreCase(userType)) { // Case-insensitive comparison
                model.addAttribute("showRSVP", true);
                model.addAttribute("showShare", true);

                boolean hasRSVPd = rsvpService.hasUserRSVPd(authenticatedUser.getUserEmail(), eventId);
                model.addAttribute("userHasRSVPd", hasRSVPd);

                Event event = eventService.getEventById(eventId);
                boolean eventHasPassed = event.getEventDate().isBefore(LocalDate.now());

                // Only show feedback if user RSVP'd AND event has passed
                boolean canLeaveFeedback = hasRSVPd && eventHasPassed;
                model.addAttribute("showFeedback", canLeaveFeedback);
                model.addAttribute("eventHasPassed", eventHasPassed);

                if (canLeaveFeedback) {
                    boolean hasSubmittedFeedback = feedbackService.hasUserSubmittedFeedback(eventId, authenticatedUser.getUserEmail());
                    model.addAttribute("hasSubmittedFeedback", hasSubmittedFeedback);
                    
                    // If user has submitted feedback, retrieve and display it
                    if (hasSubmittedFeedback) {
                        List<Feedback> userFeedbacks = feedbackService.getFeedbackForUserAndEvent(eventId, authenticatedUser.getUserEmail());
                        if (!userFeedbacks.isEmpty()) {
                            model.addAttribute("userFeedback", userFeedbacks.get(0));
                        }
                    }
                }
            } else if ("Administrator".equalsIgnoreCase(userType) || "Club_Organiser".equalsIgnoreCase(userType)) {
                model.addAttribute("showEdit", true);
                model.addAttribute("showDelete", true);
                model.addAttribute("showAddPicture", true);
                model.addAttribute("showShare", true);
              
                model.addAttribute("showAllFeedback", true);
                List<Feedback> allFeedbacks = feedbackService.getFeedbackForEvent(eventId);
                model.addAttribute("allFeedback", allFeedbacks);              
            }
        }
            Event event = eventService.getEventById(eventId);
            Club club = clubService.getClubById(event.getClub().getClubId());
            
             // Get all photos for this event
            List<Photo> eventPhotos = photoService.getPhotosByEventId(eventId);
            
            model.addAttribute("club", club);
            model.addAttribute("event", event);
            model.addAttribute("eventPhotos", eventPhotos);
            model.addAttribute("feedback", new Feedback());

        return "event-details";
    } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
        return "event-details";
    }
}

    /**
     * @brief Endpoint to retrieve and display a photo by ID
     * Returns the image data as a byte array
     */
    @GetMapping("/photo/{photoId}")
    @ResponseBody
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long photoId) {
        try {
            Photo photo = photoService.getPhotoById(photoId);
            
            if (photo == null || photo.getImageData() == null) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            // Set content type - defaulting to JPEG, adjust if needed
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(photo.getImageData().length);
            headers.setCacheControl("max-age=3600"); // Cache for 1 hour
            
            return new ResponseEntity<>(photo.getImageData(), headers, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @brief Shows the delete confirmation page for a specific event.
     * Only accessible to club organisers and administrators.
     *
     * @param eventId the ID of the event to potentially delete
     * @param model the model to bind attributes to the view
     * @param request the HTTP request to get authenticated user
     * @return the name of the delete confirmation template
     */
    @GetMapping("/event/{eventId}/delete")
    public String showDeleteConfirmation(@PathVariable Long eventId, Model model, HttpServletRequest request) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");

            if(authenticatedUser == null) {
                model.addAttribute("error", "You must be logged in to delete events.");
                return "redirect:/sign-in";
            }
            String userType = authenticatedUser.getUserType();
            if (!"Club_Organiser".equals(userType) && 
                !"Administrator".equals(userType)) {
                model.addAttribute("error", "Access denied. You don't have permission to delete events.");
                return "redirect:/event/" + eventId;
            }
            
            Event event = eventService.getEventById(eventId);
            model.addAttribute("event", event);
            model.addAttribute("userRole", userType);
            return "delete-page";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/event/all";
        }
    }

    /**
     * @brief Handles the actual deletion of an event after confirmation.
     * Only accessible to club organisers and administrators.
     *
     * @param eventId the ID of the event to delete
     * @param model the model to bind attributes to the view
     * @param request the HTTP request to get authenticated user
     * @return redirect to the events list page
     */
    @PostMapping("/event/{eventId}/delete")
    public String deleteEvent(@PathVariable Long eventId, Model model, HttpServletRequest request) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");

            if (authenticatedUser == null) {
                model.addAttribute("error", "You must be logged in to delete events.");
                return "redirect:/sign-in";
            }

            String userType = authenticatedUser.getUserType();
            
            if (!"Club_Organiser".equals(userType) && 
                !"Administrator".equals(userType)) {
                model.addAttribute("error", "Access denied. You don't have permission to delete events.");
                return "redirect:/event/" + eventId;
            }

            Event eventToDelete = eventService.getEventById(eventId);
            if (eventToDelete == null) {
                model.addAttribute("error", "Event not found");
                return "redirect:/event/all";
            }

            List<String> rsvpedUserEmails = rsvpService.getUserEmailsForEvent(eventId);
            
            if (!rsvpedUserEmails.isEmpty()) {
                sendCancellationEmails(rsvpedUserEmails, eventToDelete);
            }
            
            boolean deleted = eventService.deleteEventById(eventId);
            
            if (deleted) {
                model.addAttribute("success", "Event deleted successfully");
                return "redirect:/event/all";
            } else {
                model.addAttribute("error", "Event not found or could not be deleted");
                return "redirect:/event/" + eventId;
            }
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete event: " + e.getMessage());
            return "redirect:/event/" + eventId;
        }
    }

    /**
     * @brief Shows the edit form for a specific event.
     * Only accessible to administrators and club organisers.
     *
     * @param eventId the ID of the event to edit
     * @param model the model to bind attributes to the view
     * @param request the HTTP request to get authenticated user
     * @return the name of the event edit template
     */
    @GetMapping("/event/{eventId}/edit")
    public String showEditEventForm(@PathVariable Long eventId, Model model, HttpServletRequest request) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            
            if (!"Administrator".equals(authenticatedUser.getUserType()) && 
                !"Club_Organiser".equals(authenticatedUser.getUserType())) {
                model.addAttribute("error", "Access denied. You don't have permission to edit events.");
                return "redirect:/event/" + eventId;
            }
            
            Event event = eventService.getEventById(eventId);
            model.addAttribute("event", event);
            
            return "event-edit";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/event/" + eventId;
        }
    }

    /**
     * @brief Sends cancellation email notifications to users who RSVP'd to a deleted event.
     * 
     * @param userEmails list of email addresses of users who RSVP'd
     * @param cancelledEvent the event that was cancelled/deleted
     */
    private void sendCancellationEmails(List<String> userEmails, Event cancelledEvent) {
        try {
            String subject = "Event Cancelled: " + cancelledEvent.getEventTitle() + " | EventHub RMIT";
            
            for (String userEmail : userEmails) {
                org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
                ctx.setVariable("userName", userEmail); 
                ctx.setVariable("eventTitle", cancelledEvent.getEventTitle());
                ctx.setVariable("eventDateTime", cancelledEvent.getEventDate().toString());
                ctx.setVariable("eventLocation", cancelledEvent.getLocation());
                ctx.setVariable("cancellationReason", "The event organiser has cancelled this event.");

                String htmlBody = templateEngine.process("email_templates/cancellation.html", ctx);
                emailService.sendEmail(userEmail, subject, htmlBody);
            }
        } catch (Exception e) {
            System.err.println("Failed to send cancellation emails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @brief Handles the update of an existing event.
     * Processes the edit form submission and updates the event.
     *
     * @param eventId the ID of the event to update
     * @param event the updated event object from the form
     * @param model the model to bind attributes to the view
     * @param request the HTTP request to get authenticated user
     * @return redirect to the event details page
     */
    @PostMapping("/event/{eventId}/update")
    public String updateEvent(@PathVariable Long eventId, @ModelAttribute Event event, Model model, HttpServletRequest request) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            
            // Check if user has permission to edit events
            if (!"Administrator".equals(authenticatedUser.getUserType()) && 
                !"Club_Organiser".equals(authenticatedUser.getUserType())) {
                model.addAttribute("error", "Access denied. You don't have permission to edit events.");
                return "redirect:/event/" + eventId;
            }
            
            event.setEventId(eventId);            
            Event updatedEvent = eventService.editEventById(eventId, event);
            model.addAttribute("event", updatedEvent);            
            return "redirect:/event/" + eventId;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("event", event);
            return "event-edit";
        }
    }

    /**
     * @brief Handles RSVP toggle for an event.
     * Toggles between RSVP and un-RSVP based on current user status.
     *
     * @param eventId the ID of the event to RSVP to
     * @param model the model to bind attributes to the view
     * @param request the HTTP request to get authenticated user
     * @return redirect back to the event details page
     */
    @PostMapping("/event/{eventId}/rsvp")
    public String toggleRSVP(@PathVariable Long eventId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            
            if (authenticatedUser == null) {
                redirectAttributes.addFlashAttribute("error", "You must be logged in to RSVP to events.");
                return "redirect:/sign-in";
            }
            
            if (!"Student".equals(authenticatedUser.getUserType())) {
                redirectAttributes.addFlashAttribute("error", "Only students can RSVP to events.");
                return "redirect:/event/" + eventId;
            }

            Event event = eventService.getEventById(eventId);
            if (event.getEventDate().isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Cannot RSVP to past events.");
                return "redirect:/event/" + eventId;
            }
            
            boolean isNowRSVPd = rsvpService.toggleRSVP(authenticatedUser.getUserEmail(), eventId);
            
            if (isNowRSVPd) {

                redirectAttributes.addFlashAttribute("success", "Successfully RSVP'd to the event! Confirmation email sent.");
                
                String userName = authenticatedUser.getUserEmail();
                String userEmail = authenticatedUser.getUserEmail();
                String eventTitle = event.getEventTitle();
                String eventDateTime = event.getEventDate().toString();
                String eventLocation = event.getLocation();
                String eventLink = "https://localhost:8080/event/" + eventId; 
                String subject = "Your RSVP to " + eventTitle + " is confirmed! EventHub | RMIT";
                
                if (userName == null || userEmail == null || eventTitle == null || eventDateTime == null || eventLocation == null) {
                    redirectAttributes.addFlashAttribute("error", "Could not send confirmation email: missing event or user information.");
                    return "redirect:/event/" + eventId;
                }

                org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
                ctx.setVariable("userName", userName);
                ctx.setVariable("eventTitle", eventTitle);
                ctx.setVariable("eventDateTime", eventDateTime);
                ctx.setVariable("eventLocation", eventLocation);
                ctx.setVariable("eventLink", eventLink);

                String htmlBody = templateEngine.process("email_templates/rsvp.html", ctx);
                
                emailService.sendEmail(userEmail, subject, htmlBody);

            } else {
                redirectAttributes.addFlashAttribute("success", "RSVP removed successfully.");
            }


            
            return "redirect:/event/" + eventId;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to process RSVP: " + e.getMessage());
            return "redirect:/event/" + eventId;
        }
    }

    /**
     * @brief Retrieves all events that a user has RSVP'd to.
     * This method handles authentication and delegates the business logic
     * to the RSVPService for retrieving RSVP events.
     * 
     * @param userEmail the email of the user whose RSVP events to retrieve
     * @param model the model to pass data to the view
     * @param request the HTTP request object to check authentication
     * @param redirectAttributes attributes for redirect scenarios
     * @return the view name for displaying RSVP events, or redirect on error/unauthorized
     * 
     * @author Lucas Aponso
     * @version 1.0
     */
    @GetMapping("/event/rsvp/{userEmail}")
    public String getRSVPEvents(@PathVariable String userEmail, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            if (authenticatedUser == null) {
                redirectAttributes.addFlashAttribute("error", "You must be logged in to view RSVP events.");
                return "redirect:/sign-in";
            }

            // Delegate business logic to service layer
            List<Event> rsvpEvents = rsvpService.getRSVPEventsForUser(userEmail);

            model.addAttribute("rsvpEvents", rsvpEvents);
            model.addAttribute("userEmail", userEmail);

            return "rsvp-events";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while retrieving your RSVP events.");
            return "redirect:/events";
        }
    }

    /**
     * @brief Handles submission of feedback for a specific event.
     * Validates user authentication and feedback data before saving.
     * @param eventId the ID of the event to submit feedback for
     * @param feedbackText the feedback text submitted by the user
     * @param rating the rating submitted by the user (1-5)
     * @param model the model to pass data to the view
     * @param request the HTTP request object to check authentication
     * @param redirectAttributes attributes for redirect scenarios
     * @return redirect to the event details page with success or error message
     * @author Yunie Cho
     * @version 1.0
     */
    @PostMapping("/event/{eventId}/feedback")
public String submitFeedback(@PathVariable Long eventId, 
                             @RequestParam String feedbackText,
                             @RequestParam Integer rating,
                             HttpServletRequest request,
                             Model model,
                             RedirectAttributes redirectAttributes) {  // Add RedirectAttributes
    try {
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        if (authenticatedUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to submit feedback.");
            return "redirect:/sign-in";
        }

        // Case-insensitive role check
        if (!"student".equalsIgnoreCase(authenticatedUser.getUserType())) {
            redirectAttributes.addFlashAttribute("error", "You must be a registered student to submit feedback.");
            return "redirect:/sign-in";
        }

        Event event = eventService.getEventById(eventId);
        if (event == null) {
            redirectAttributes.addFlashAttribute("error", "Event not found.");
            return "redirect:/events";
        }

        // Check if event is past
        if (!event.getEventDate().isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error", "Feedback can only be submitted for past events.");
            return "redirect:/event/" + eventId + "?feedback=error";
        }

        // Check RSVP
        if (!rsvpService.hasUserRSVPd(authenticatedUser.getUserEmail(), eventId)) {
            redirectAttributes.addFlashAttribute("error", "You must RSVP to this event to submit feedback.");
            return "redirect:/event/" + eventId + "?feedback=error";
        }

        // Check for duplicate feedback
        if (feedbackService.hasUserSubmittedFeedback(eventId, authenticatedUser.getUserEmail())) {
            redirectAttributes.addFlashAttribute("error", "You have already submitted feedback for this event.");
            return "redirect:/event/" + eventId + "?feedback=error";
        }

        // Validate input
        String trimmedText = feedbackText.trim();
        if (trimmedText.isEmpty() || rating == null || rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("error", "Invalid feedback text or rating (1-5 stars required).");
            return "redirect:/event/" + eventId + "?feedback=error";
        }

        // Create and save feedback
        Feedback feedback = new Feedback();
        feedback.setEventId(eventId);
        feedback.setUserEmail(authenticatedUser.getUserEmail());
        feedback.setFeedbackText(trimmedText);
        feedback.setRating(rating);
        feedback.setSubmissionDate(LocalDateTime.now());
        feedbackService.createFeedback(feedback);

        redirectAttributes.addFlashAttribute("successMessage", "Feedback submitted successfully!");
        return "redirect:/event/" + eventId + "?feedback=success";

    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Failed to submit feedback: " + e.getMessage());
        return "redirect:/event/" + eventId + "?feedback=error";
    }
}
    
}