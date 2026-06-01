package au.edu.rmit.sept.webapp.User;

import au.edu.rmit.sept.webapp.model.User;
import java.time.LocalDateTime;

/**
 * @brief Helper class to create {@link User} instances for testing purposes.
 */
public class UserHelperTest {

    /**
     * Creates a test {@link User} object with the given parameters.
     *
     * @param email the user email
     * @param firstName the user's first name
     * @param surname the user's surname
     * @param password the user's password
     * @param role the user's role
     * @return a fully constructed {@link User} object
     */
    public static User createUser(String email, String firstName, String surname, 
                                 String password, String role) {
        User user = new User();
        user.setUserEmail(email);
        user.setUserName(firstName + " " + surname);
        user.setUserPassword(password);
        user.setUserType(role);
        user.setUserDeactivationStatus(false);
        user.setUserEventCounter(0);
        user.setUserJoinDate(LocalDateTime.now());
        
        // Set form binding fields
        user.setFirstName(firstName);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        
        return user;
    }

    /**
     * Creates a default user with common dummy values.
     * @param email the user email
     * @param role the user role
     * @return a default {@link User} with preset dummy fields
     */
    public static User createDefaultUser(String email, String role) {
        return createUser(
            email,
            "John",
            "Doe",
            "Password123",
            role
        );
    }

    /**
     * Creates a user for registration testing with form fields.
     * @param email the user email
     * @param firstName the first name
     * @param surname the surname
     * @param password the password
     * @param confirmPassword the password confirmation
     * @param role the user role
     * @return a {@link User} with form binding fields set
     */
    public static User createRegistrationUser(String email, String firstName, String surname,
                                            String password, String confirmPassword, String role) {
        User user = new User();
        user.setFirstName(firstName);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPassword(password);
        user.setConfirmPassword(confirmPassword);
        user.setRole(role);
        return user;
    }
}
