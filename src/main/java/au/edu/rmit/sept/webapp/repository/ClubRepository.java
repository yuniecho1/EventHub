package au.edu.rmit.sept.webapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.Club;

/**
 * @brief Repository interface for {@link Club} entities.
 * Extends JpaRepository to provide CRUD operations and
 * additional query methods for Club objects.
 * 
 * @author Lucas Aponso
 * @version 1.0
 */
@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
    /**
     * @brief Finds clubs by their unique event ID.
     * 
     * @param eventId the unique identifier of the club
     * @return a list of {@link Club} objects matching the given event ID
     */
    Club findByClubId(Long eventId);


    /**
     * @brief Finds all clubs associated with the specified organiser's email.
     *
     * @param userEmail the email address of the organiser
     * @return a list of {@link Club} entities managed by the given organiser
     */
    List<Club> findByUserEmail(String userEmail);

    /**
     * @brief Finds a club by its unique name.
     *
     * @param clubName the name of the club
     * @return an {@link Optional} containing the club if found, otherwise empty
     */
    Optional<Club> findByClubName(String clubName);

    
    /**
     * @brief Retrieves all club names, sorted alphabetically.
     *
     * @return a list of club names
     */
    @Query("SELECT c.clubName FROM Club c ORDER BY c.clubName")
    List<String> findAllClubNames();

    /**
     * @brief Retrieves all distinct tags assigned to clubs.
     *
     * @return a list of unique club tags
     */
    @Query("SELECT DISTINCT c.clubTag FROM Club c")
    List<String> findAllDistinctTags();

    
    /**
     * @brief Retrieves all distinct organiser email addresses.
     *
     * @return a list of unique organiser emails
     */
    @Query(value = "SELECT DISTINCT c.userEmail FROM Club c")
    List<String> findAllDistinctOrganisers();

    /**
     * @brief Retrieves all clubs sorted alphabetically by name.
     *
     * @return a list of clubs ordered by club name
     */
    List<Club> findAllByOrderByClubNameAsc();

    
    /**
     * @brief Searches for clubs by query and filters them by tag or organiser.
     *
     * Matches clubs where the name, description, or tag contains the search query.
     * Allows optional filtering by tag or organiser email.
     *
     * @param query the search string to match against club name, description, or tag
     * @param filteredTag optional tag filter (case-insensitive)
     * @param filteredOrganiser optional organiser email filter (case-insensitive)
     * @return a list of clubs matching the search and filter criteria
     */
    @Query("SELECT c FROM Club c JOIN User u ON c.userEmail = u.userEmail " +
        "WHERE (:query IS NULL OR LOWER(c.clubName) LIKE LOWER(CONCAT('%', :query, '%')) " +
        "      OR LOWER(c.clubDescription) LIKE LOWER(CONCAT('%', :query, '%')) " +
        "      OR LOWER(c.clubTag) LIKE LOWER(CONCAT('%', :query, '%'))) " +
        "AND (:filteredTag IS NULL OR LOWER(c.clubTag) = LOWER(:filteredTag))" +
        "AND (:filteredOrganiser IS NULL OR LOWER(u.userEmail) = LOWER(:filteredOrganiser)) ")
    List<Club> searchAndFilterClubs(
            @Param("query") String query,
            @Param("filteredTag") String filteredTag,
            @Param("filteredOrganiser") String filteredOrganiser
    );
}