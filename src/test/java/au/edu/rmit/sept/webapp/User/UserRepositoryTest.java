package au.edu.rmit.sept.webapp.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;

/**
 * @brief Integration test class for {@link UserRepository}.
 *        Verifies CRUD operations, existence checks, and search functionality.
 * 
 * Ensures:
 * - Users can be saved, retrieved, deleted, and searched correctly.
 * - Case-insensitive queries and unique constraints function as expected.
 * 
 * @author
 * @version 1.0
 */
@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    /**
     * @brief Verifies that a {@link User} entity can be saved and retrieved by email.
     * 
     * Scenario:
     * - Create and save a user.
     * - Retrieve the user by email.
     * - Assert that it exists and that name and type match the expected values.
     */
    @Test
    void saveAndFindUser_shouldReturnUser_whenValidUserData() {
        User user = new User();
        user.setUserEmail("testuser@example.com");
        user.setUserName("Test User");
        user.setUserPassword("Password1234");
        user.setUserType("Student");
        user.setUserDeactivationStatus(false);
        user.setUserEventCounter(0);
        user.setUserJoinDate(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        Optional<User> found = userRepository.findByUserEmail(savedUser.getUserEmail());

        assertThat(found).isPresent();
        assertThat(found.get().getUserName()).isEqualTo("Test User");
        assertThat(found.get().getUserType()).isEqualTo("Student");
    }

    /**
     * @brief Verifies that {@link UserRepository#existsByUserEmail(String)} returns true
     *        when a user exists with the specified email.
     * 
     * Scenario:
     * - Save a user.
     * - Check existence by email.
     * - Assert that the result is true.
     */
    @Test
    void existsByUserEmail_shouldReturnTrue_whenUserExists() {
        User user = new User();
        user.setUserEmail("existsuser@example.com");
        user.setUserName("Exists User");
        user.setUserPassword("Password1234");
        user.setUserType("Administrator");
        user.setUserDeactivationStatus(false);
        user.setUserEventCounter(0);
        user.setUserJoinDate(LocalDateTime.now());

        userRepository.save(user);
        boolean exists = userRepository.existsByUserEmail("existsuser@example.com");
        assertThat(exists).isTrue();
    }

    /**
     * @brief Verifies that {@link UserRepository#getByUserEmail(String)} returns
     *        the correct user for a given email.
     * 
     * Scenario:
     * - Save a user using a helper.
     * - Retrieve it using getByUserEmail.
     * - Assert that the correct user is returned.
     */
    @Test
    void getByUserEmail_shouldReturnUser_whenEmailMatches() {
        User testUser = UserHelperTest.createDefaultUser("email@address.com", "Student");
        userRepository.save(testUser);

        User found = userRepository.getByUserEmail(testUser.getUserEmail());
        assertThat(found).isNotNull();
        assertThat(found.getUserEmail()).isEqualTo(testUser.getUserEmail());
        assertThat(found.getUserName()).isEqualTo("John Doe");
    }

    /**
     * @brief Verifies that {@link UserRepository#deleteByEmail(String)} removes
     *        a user from the database.
     * 
     * Scenario:
     * - Save a user.
     * - Delete the user by email.
     * - Assert that the user no longer exists in the database.
     */
    @Test
    void deleteByEmail_shouldRemoveUser_whenUserExists() {
        User testUser = UserHelperTest.createDefaultUser("email@address.com", "Student");
        userRepository.save(testUser);

        int deletedCount = userRepository.deleteByEmail(testUser.getUserEmail());
        assertThat(deletedCount).isEqualTo(1);
        assertThat(userRepository.existsByUserEmail(testUser.getUserEmail())).isFalse();
    }

    /**
     * @brief Verifies that {@link UserRepository#searchUsers(String)} correctly matches
     *        users by email, name, or type (case-insensitive).
     * 
     * Scenario:
     * - Save multiple users with varying attributes.
     * - Search by email substring, name substring, and type.
     * - Assert that results are non-empty and match criteria.
     */
    @Test
    void searchUsers_shouldReturnMatchingUsers_whenQueryMatchesEmailNameOrType() {
        List<User> testUsers = new ArrayList<>();
        for (int i = 1; i < 12; i++) {
            User testUser = UserHelperTest.createUser(("email" + i), (i + "first"), "last", ("password" + i), "Student");
            testUsers.add(testUser);
            userRepository.save(testUser);
        }

        // Search by part of email (all have "email" in the address)
        List<User> byEmail = userRepository.searchUsers("email");
        assertThat(byEmail)
                .hasSizeGreaterThanOrEqualTo(11)
                .allMatch(u -> u.getUserEmail().toLowerCase().contains("email"));

        // Search by part of first name (all names contain "first")
        List<User> byName = userRepository.searchUsers("first");
        assertThat(byName)
                .hasSizeGreaterThanOrEqualTo(11)
                .allMatch(u -> u.getUserName().toLowerCase().contains("first"));

        // Search by role/type (all users have role Student)
        List<User> byRole = userRepository.searchUsers("student");
        assertThat(byRole)
                .hasSizeGreaterThanOrEqualTo(11)
                .allMatch(u -> u.getUserType().equalsIgnoreCase("Student"));
    }
}