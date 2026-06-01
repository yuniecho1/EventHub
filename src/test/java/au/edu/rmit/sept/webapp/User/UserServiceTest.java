package au.edu.rmit.sept.webapp.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.service.UserService;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Reset the database before each test to ensure isolation
     */
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    /** 
     * @brief Tests registering a valid user.
     * Verifies that the user is saved, returned, and the password is hashed.
     */
    @Test
    void registerUser_shouldSaveUser_whenDataIsValid() {
        User savedUser = userService.registerUser(
            "ValidFirstName", "ValidLastName", "valid.email@example.com",
            "ValidPassword1", "ValidPassword1", "Student"
        );

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUserEmail()).isEqualTo("valid.email@example.com");
        assertThat(savedUser.getUserName()).isEqualTo("ValidFirstName ValidLastName");
        assertThat(savedUser.getUserType()).isEqualTo("Student");
        assertThat(savedUser.getUserPassword()).isNotEqualTo("ValidPassword1");
    }

    /** 
     * @brief Tests registering a user with a null first name.
     * Verifies that an exception is thrown.
     */
    @Test
    void registerUser_shouldThrowException_whenFirstNameIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(
                null, "ValidLastName", "null.first@example.com",
                "ValidPassword1", "ValidPassword1", "Student"
            );
        });

        assertThat(exception.getMessage()).contains("First name is required");
    }

    /** 
     * @brief Tests registering a user with an invalid email.
     * Verifies that an exception is thrown.
     */
    @Test
    void registerUser_shouldThrowException_whenEmailIsInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(
                "ValidFirstName", "ValidLastName", "invalid-email",
                "ValidPassword1", "ValidPassword1", "Student"
            );
        });

        assertThat(exception.getMessage()).contains("Please enter a valid email address");
    }

    /** 
     * @brief Tests registering a user with a weak password.
     * Verifies that an exception is thrown.
     */
    @Test
    void registerUser_shouldThrowException_whenPasswordIsWeak() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(
                "ValidFirstName", "ValidLastName", "weak.pass@example.com",
                "weak", "weak", "Student"
            );
        });

        assertThat(exception.getMessage()).contains("Password must be at least 8 characters long");
    }

    /** 
     * @brief Tests registering a user with a password missing uppercase letters.
     * Verifies that an exception is thrown.
     */
    @Test
    void registerUser_shouldThrowException_whenPasswordLacksUppercase() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(
                "ValidFirstName", "ValidLastName", "no.uppercase@example.com",
                "alllowercase1", "alllowercase1", "Student"
            );
        });

        assertThat(exception.getMessage()).contains("Password must contain at least one uppercase letter");
    }

    /** 
     * @brief Tests registering a user with mismatched passwords.
     * Verifies that an exception is thrown.
     */
    @Test
    void registerUser_shouldThrowException_whenPasswordsDoNotMatch() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(
                "ValidFirstName", "ValidLastName", "password.mismatch@example.com",
                "ValidPassword1", "DifferentPassword1", "Student"
            );
        });

        assertThat(exception.getMessage()).contains("Password confirmation does not match");
    }

    /** 
     * @brief Tests registering a user with an invalid role.
     * Verifies that an exception is thrown.
     */
    @Test
    void registerUser_shouldThrowException_whenRoleIsInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(
                "ValidFirstName", "ValidLastName", "invalid.role@example.com",
                "ValidPassword1", "ValidPassword1", "invalid_role"
            );
        });

        assertThat(exception.getMessage()).contains("Invalid role selected");
    }

    /** 
     * @brief Tests registering duplicate users.
     * Verifies that an exception is thrown for duplicate email.
     */
    @Test
    void registerUser_shouldThrowException_whenEmailAlreadyExists() {
        userService.registerUser(
            "ValidFirstName", "ValidLastName", "duplicate@example.com",
            "ValidPassword1", "ValidPassword1", "Student"
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(
                "AnotherFirstName", "AnotherLastName", "duplicate@example.com",
                "AnotherPassword1", "AnotherPassword1", "Administrator"
            );
        });

        assertThat(exception.getMessage()).contains("User with this email already exists");
    }

    /** 
     * @brief Tests authenticating a valid user.
     * Verifies successful authentication and correct details.
     */
    @Test
    void authenticateUser_shouldReturnUser_whenCredentialsAreValid() {
        userService.registerUser(
            "John", "Doe", "auth@example.com",
            "Password123", "Password123", "Student"
        );

        User authenticatedUser = userService.authenticateUser("auth@example.com", "Password123");

        assertThat(authenticatedUser).isNotNull();
        assertThat(authenticatedUser.getUserEmail()).isEqualTo("auth@example.com");
        assertThat(authenticatedUser.getUserName()).isEqualTo("John Doe");
    }

    /** 
     * @brief Tests authenticating a user with wrong password.
     * Verifies that an exception is thrown.
     */
    @Test
    void authenticateUser_shouldThrowException_whenPasswordIsIncorrect() {
        userService.registerUser(
            "John", "Doe", "wrongpass@example.com",
            "Password123", "Password123", "Student"
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.authenticateUser("wrongpass@example.com", "WrongPassword");
        });

        assertThat(exception.getMessage()).contains("Invalid email or password");
    }

    /** 
     * @brief Tests authenticating a non-existent user.
     * Verifies that an exception is thrown.
     */
    @Test
    void authenticateUser_shouldThrowException_whenUserDoesNotExist() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.authenticateUser("nonexistent@example.com", "ValidPassword1");
        });

        assertThat(exception.getMessage()).contains("Invalid email or password");
    }

    /** 
     * @brief Tests creating and verifying an auth token.
     * Verifies encoding and decoding correctness.
     */
    @Test
    void createAuthToken_shouldEncodeAndDecodeCredentialsCorrectly() {
        String authToken = userService.createAuthToken("token.user@example.com", "ValidPassword1");

        assertThat(authToken).isNotNull();
        assertThat(authToken).isNotEmpty();

        String[] credentials = userService.decodeAuthToken(authToken);
        assertThat(credentials).hasSize(2);
        assertThat(credentials[0]).isEqualTo("token.user@example.com");
        assertThat(credentials[1]).isEqualTo("ValidPassword1");
    }

    /** 
     * @brief Tests verifying login with a valid token.
     * Verifies that it returns true.
     */
    @Test
    void verifyLogin_shouldReturnTrue_whenTokenIsValid() {
        userService.registerUser(
            "John", "Doe", "tokentest@example.com",
            "Password123", "Password123", "Student"
        );

        String authToken = userService.createAuthToken("tokentest@example.com", "Password123");

        boolean isValid = userService.verifyLogin(authToken);
        assertThat(isValid).isTrue();
    }

    /** 
     * @brief Tests verifying login with an invalid token.
     * Verifies that it returns false.
     */
    @Test
    void verifyLogin_shouldReturnFalse_whenTokenIsInvalid() {
        String invalidToken = "invalid.token.value";
        boolean isValid = userService.verifyLogin(invalidToken);
        assertThat(isValid).isFalse();
    }

    /** 
     * @brief Tests retrieving a user by a valid token.
     * Verifies that the correct user is returned.
     */
    @Test
    void getUserByAuthToken_shouldReturnUser_whenTokenIsValid() {
        userService.registerUser(
            "ValidFirstName", "ValidLastName", "get.user@example.com", "ValidPassword1", "ValidPassword1", "Administrator"
        );

        String authToken = userService.createAuthToken("get.user@example.com", "ValidPassword1");

        User user = userService.getUserByAuthToken(authToken);

        assertThat(user).isNotNull();
        assertThat(user.getUserEmail()).isEqualTo("get.user@example.com");
        assertThat(user.getUserType()).isEqualTo("Administrator");
    }

    /** 
     * @brief Tests retrieving a user with an invalid token.
     * Verifies that an exception is thrown.
     */
    @Test
    void getUserByAuthToken_shouldThrowException_whenTokenIsInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserByAuthToken("invalid.token.value");
        });

        assertThat(exception.getMessage()).contains("Invalid auth token");
    }

    /** 
     * @brief Tests retrieving all users.
     * Verifies that the list is alphabetically sorted.
     */
    @Test
    void getAllUsers_shouldReturnAlphabeticallySortedList_whenUsersExist() {
        userService.registerUser("Charlie", "ZZZ", "charlie@example.com", "Password1A", "Password1A", "Student");
        userService.registerUser("Alice", "ZZZ", "alice@example.com", "Password1A", "Password1A", "Student");
        userService.registerUser("Bob", "ZZZ", "bob@example.com", "Password1A", "Password1A", "Student");

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(3);
        assertThat(users)
                .extracting(User::getUserName)
                .containsExactly("Alice ZZZ", "Bob ZZZ", "Charlie ZZZ");
    }

    /** 
     * @brief Tests retrieving a user by email.
     * Verifies that the correct user is returned.
     */
    @Test
    void getUserByEmail_shouldReturnUser_whenEmailExists() {
        User saved = userService.registerUser(
            "Diana", "Prince", "diana@example.com",
            "Password1A", "Password1A", "Student");

        User found = userService.getUserByEmail("diana@example.com");
        assertThat(found).isNotNull();
        assertThat(found.getUserEmail()).isEqualTo(saved.getUserEmail());
    }

    /** 
     * @brief Tests retrieving a user by email that doesn’t exist.
     * Verifies that it returns null.
     */
    @Test
    void getUserByEmail_shouldReturnNull_whenEmailDoesNotExist() {
        assertThat(userService.getUserByEmail("missing@example.com")).isNull();
    }

    /** 
     * @brief Tests deleting an existing user.
     * Verifies that the user is removed and true is returned.
     */
    @Test
    void deleteUserByEmail_shouldReturnTrue_whenUserExists() {
        userService.registerUser("Eve", "Smith", "eve@example.com",
                "Password1A", "Password1A", "Student");

        boolean deleted = userService.deleteUserByEmail("eve@example.com");
        assertThat(deleted).isTrue();
        assertThat(userRepository.findByUserEmail("eve@example.com")).isEmpty();
    }

    /** 
     * @brief Tests deleting a non-existent user.
     * Verifies that it returns false.
     */
    @Test
    void deleteUserByEmail_shouldReturnFalse_whenUserDoesNotExist() {
        boolean deleted = userService.deleteUserByEmail("nosuch@example.com");
        assertThat(deleted).isFalse();
    }

    /** 
     * @brief Tests editing an existing user.
     * Verifies that all fields are updated correctly.
     */
    @Test
    void editUserByEmail_shouldUpdateFields_whenUserExists() {
        userService.registerUser("Frank", "Miller", "frank@example.com",
                "Password1A", "Password1A", "Student");

        User changes = new User();
        changes.setUserName("Franklin M");
        changes.setUserPassword("newHashedPassword");
        changes.setUserType("Administrator");
        changes.setUserDeactivationStatus(true);
        changes.setUserEventCounter(7);

        User updated = userService.editUserByEmail("frank@example.com", changes);

        assertThat(updated.getUserName()).isEqualTo("Franklin M");
        assertThat(updated.getUserPassword()).isEqualTo("newHashedPassword");
        assertThat(updated.getUserType()).isEqualTo("Administrator");
        assertThat(updated.getUserDeactivationStatus()).isTrue();
        assertThat(updated.getUserEventCounter()).isEqualTo(7);
    }

    /** 
     * @brief Tests editing a non-existent user.
     * Verifies that an exception is thrown.
     */
    @Test
    void editUserByEmail_shouldThrowException_whenUserDoesNotExist() {
        User dummy = new User();
        dummy.setUserName("Ghost User");
        dummy.setUserPassword("dummyPassword");
        dummy.setUserType("Student");
        dummy.setUserDeactivationStatus(false);
        dummy.setUserEventCounter(0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.editUserByEmail("missing@example.com", dummy));

        assertThat(ex.getMessage()).contains("User with email missing@example.com not found");
    }

    /** 
     * @brief Tests searching users with null or empty input.
     * Verifies that all users are returned.
     */
    @Test
    void searchUsers_shouldReturnAll_whenQueryIsNullOrEmpty() {
        userService.registerUser("Helen", "ZZZ", "helen@example.com", "Password1A", "Password1A", "Student");
        userService.registerUser("Ian", "ZZZ", "ian@example.com", "Password1A", "Password1A", "Student");

        List<User> all1 = userService.searchUsers(null);
        List<User> all2 = userService.searchUsers("   ");

        assertThat(all1).hasSize(2);
        assertThat(all2).hasSize(2);
    }
}