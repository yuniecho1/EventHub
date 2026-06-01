package au.edu.rmit.sept.webapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.ClubRepository;

/**
 * @brief Service class for managing {@link Club} entities.
 * Provides business logic for creating, retrieving, and filtering clubs,
 * including validation and duplicate checks.
 * 
 * Handles access control for club creation based on user roles.
 * Delegates database operations to {@link ClubRepository}.
 * 
 * @author Lucas
 * @version 1.0
 */
@Service
public class ClubService {

    @Autowired
    private ClubRepository clubRepository;

    /**
     * @brief Validates that all required fields of a {@link Club} are non-null.
     * 
     * Throws an exception if any of the fields (name, description, tag) are null.
     * 
     * @param club the club to validate
     * @throws IllegalArgumentException if any required field is null
     */
    public void validateClubNotNull(Club club) {
        if (club.getClubName() == null) {
            throw new IllegalArgumentException("Club Name cannot be null");
        }
        if (club.getClubDescription() == null) {
            throw new IllegalArgumentException("Club description cannot be null");
        }
        if (club.getClubTag() == null) {
            throw new IllegalArgumentException("Club tags cannot be null");
        }
    }

    /**
     * @brief Creates a new {@link Club} in the repository after validation.
     * 
     * Validates that the authenticated user is allowed to create clubs, ensures
     * no duplicate club exists with the same name, and checks required fields.
     * 
     * @param club the club to create
     * @param authUser the authenticated user performing the creation
     * @return the saved {@link Club} entity
     * @throws IllegalStateException if the user is not authenticated, is a student,
     *                               or if a club with the same name already exists
     */
    @Transactional
    public Club createClub(Club club, User authUser) {
        if (authUser == null) {
            throw new IllegalStateException("User is not authenticated.");
        }

        if ("student".equals(authUser.getUserType())) {
            throw new IllegalStateException("Students are not allowed to create clubs.");
        }

        club.setUserEmail(authUser.getUserEmail());

        if (clubRepository.findByClubName(club.getClubName()).isPresent()) {
            throw new IllegalStateException("Club already exists with the same name");
        }

        validateClubNotNull(club);

        return clubRepository.save(club);
    }

    /**
     * @brief Retrieves clubs filtered by query, tag, and organiser.
     * 
     * @param query optional search string for club name, description, or tag
     * @param filteredTag optional tag filter
     * @param filteredOrganiser optional organiser email filter
     * @return a list of {@link Club} entities matching the search and filters
     */
    @Transactional
    public List<Club> searchAndFilterClubs(String query, String filteredTag, String filteredOrganiser) {
        if (query != null && query.trim().isEmpty()) query = null;
        return clubRepository.searchAndFilterClubs(query, filteredTag, filteredOrganiser);
    }

    /**
     * @brief Searches clubs by query string.
     * 
     * If query is null or empty, returns all clubs sorted by name.
     * 
     * @param query the search string
     * @return a list of {@link Club} entities matching the query
     */
    @Transactional
    public List<Club> searchClubs(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getClubs();
        }
        return clubRepository.searchAndFilterClubs(query, null, null);
    }

    /**
     * @brief Retrieves all clubs sorted alphabetically by club name.
     * 
     * @return a list of all {@link Club} entities
     */
    @Transactional
    public List<Club> getClubs() {
        return clubRepository.findAllByOrderByClubNameAsc();
    }

    /**
     * @brief Retrieves all club names.
     * 
     * @return a list of club names as {@link String}
     */
    public List<String> findAllClubNames() {
        return clubRepository.findAllClubNames();
    }

    /**
     * @brief Retrieves all distinct club tags.
     * 
     * @return a list of unique tags as {@link String}
     */
    @Transactional
    public List<String> findAllTags() {
        return clubRepository.findAllDistinctTags();
    }

    /**
     * @brief Retrieves all distinct organiser emails.
     * 
     * @return a list of unique organiser emails as {@link String}
     */
    @Transactional
    public List<String> findAllOrganisers() {
        return clubRepository.findAllDistinctOrganisers();
    }

    /**
     * @brief Retrieves an event by its ID.
     * 
     * @param eventId the ID of the event
     * @return the {@link Event} entity with the given ID
     */
    @Transactional
    public Club getClubById(Long eventId) {
        return clubRepository.findByClubId(eventId);
    }
}