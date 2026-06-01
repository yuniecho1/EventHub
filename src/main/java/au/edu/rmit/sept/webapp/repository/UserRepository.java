package au.edu.rmit.sept.webapp.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.User;
import jakarta.transaction.Transactional;

/**
 * @brief Repository interface for {@link User} entities.
 * Extends JpaRepository to provide CRUD operations and
 * additional query methods for User objects.
 * 
 * Provides methods for finding, checking existence, deleting, and searching
 * users based on email, name, or role.
 * 
 * @author  Zac Spongberg
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * @brief Checks whether a user exists
     * 
     * @param email the unique email of the user to find
     * @return boolean
     */
    boolean existsByUserEmail(String email);

    /**
     * @brief Finds a user by their unique email address.
     *
     * @param email the email of the user to find
     * @return an Optional containing the {@link User} if found, or empty if not
     */
    Optional <User> findByUserEmail(String email);

    /**
     * @brief Retrieves a user by their email.
     *
     * @param email the email of the user to retrieve
     * @return the {@link User} object matching the given email. Use if the user is known to exist.
     */
    User getByUserEmail(String email);

    /**
     * @brief Deletes a user by their email.
     *
     * @param userEmail the email of the user to delete
     * @return the number of entities deleted
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.userEmail = :userEmail")
    int deleteByEmail(@Param("userEmail") String userEmail);

    /**
     * @brief Searches for users based on a query string.
     * Matches against userEmail, userName, or userType (case-insensitive).
     *
     * @param query the search string
     * @return a list of {@link User} objects matching the search criteria
     */
    @Query("SELECT u FROM User u " +
           "WHERE LOWER(u.userEmail) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "   OR LOWER(u.userName)  LIKE LOWER(CONCAT('%', :query, '%')) " +
           "   OR LOWER(u.userType)  LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchUsers(@Param("query") String query);
}
