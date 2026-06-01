package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * @brief Service class for managing {@link RSVP} entities.
 * Provides business logic for RSVP operations including checking
 * user RSVP status and toggling RSVP responses.
 * 
 * @author Agampreet Singh
 * @version 1.0
 */
@Service
public class RSVPService {

    @Autowired
    private RSVPRepository rsvpRepository;
    
    @Autowired
    private EventService eventService;

    /**
     * @brief Checks if a user has already RSVP'd to a specific event.
     *
     * @param userEmail the email of the user
     * @param eventId the ID of the event
     * @return true if user has RSVP'd, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean hasUserRSVPd(String userEmail, Long eventId) {
        RSVP rsvp = rsvpRepository.findByUserEmailAndEventId(userEmail, eventId);
        boolean hasRSVPd = (rsvp != null);
        return hasRSVPd;
    }

    /**
     * @brief Toggles RSVP status for a user and event.
     * If user has already RSVP'd, removes the RSVP (table entry).
     * If user hasn't RSVP'd, adds the user to the RSVP list (table).
     *
     * @param userEmail the email of the user
     * @param eventId the ID of the event
     * @return true if RSVP was created, false if RSVP was removed
     */
    @Transactional
    public boolean toggleRSVP(String userEmail, Long eventId) {
        if (hasUserRSVPd(userEmail, eventId)) {
            removeRSVP(userEmail, eventId);
            return false;
        } else {
            createRSVP(userEmail, eventId);
            return true;
        }
    }

    /**
     * @brief Creates a new RSVP for a user and event.
     *
     * @param userEmail the email of the user
     * @param eventId the ID of the event
     * @return the created RSVP entity
     */
    @Transactional
    public RSVP createRSVP(String userEmail, Long eventId) {
        RSVP rsvp = new RSVP(userEmail, eventId);
        return rsvpRepository.save(rsvp);
    }

    /**
     * @brief Removes an RSVP for a specific user and event.
     *
     * @param userEmail the email of the user
     * @param eventId the ID of the event
     */
    @Transactional
    public void removeRSVP(String userEmail, Long eventId) {
        List<RSVP> rsvps = rsvpRepository.findAllByEventId(eventId);
        rsvps.stream()
            .filter(rsvp -> userEmail.equals(rsvp.getUserEmail()))
            .forEach(rsvpRepository::delete);
    }

    /**
     * @brief Gets all RSVPs for a specific event.
     *
     * @param eventId the ID of the event
     * @return list of RSVPs for the event
     */
    @Transactional(readOnly = true)
    public List<RSVP> getRSVPsForEvent(Long eventId) {
        return rsvpRepository.findAllByEventId(eventId);
    }

    // **
    //  * @brief Gets all RSVPs for a specific user.
    //  * 
    //  * @param userEmail the email of the user
    //  * @return list of RSVPs for the user
    //  */
    @Transactional(readOnly = true)
    public List<RSVP> getRSVPsForUser(String userEmail) {
        return rsvpRepository.findAllByUserEmail(userEmail);
    }

    /**
     * @brief Gets all events that a user has RSVP'd to.
     * This method retrieves all RSVP records for a user and then
     * fetches the corresponding Event objects.
     *
     * @param userEmail the email of the user
     * @return list of events the user has RSVP'd to
     */
    @Transactional(readOnly = true)
    public List<Event> getRSVPEventsForUser(String userEmail) {
        List<RSVP> rsvps = getRSVPsForUser(userEmail);
        List<Event> rsvpEvents = new ArrayList<>();
        
        for (RSVP rsvp : rsvps) {
            Event event = eventService.getEventById(rsvp.getEventId());
            if (event != null) {
                rsvpEvents.add(event);
            }
        }
        
        return rsvpEvents;
    }

    /**
     * @brief Retrieves all user emails who have RSVP'd to a specific event.
     * 
     * @param eventId the ID of the event
     * @return list of user email addresses who RSVP'd to the event
     */
    public List<String> getUserEmailsForEvent(Long eventId) {
        List<RSVP> rsvps = rsvpRepository.findByEventId(eventId);
        return rsvps.stream()
                .map(RSVP::getUserEmail)
                .collect(Collectors.toList());
    }

}