package au.edu.rmit.sept.webapp.service;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;

/**
 * @brief Service class for managing {@link User} entities.
 * Provides business logic for user registration, authentication, editing,
 * deletion, and retrieval. Handles password hashing, auth token management,
 * and input validation.
 * 
 * @author Lucas Aponso
 * @version 1.0
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    /**
     * @brief Registers a new user with validation.
     * 
     * @param firstName user's first name
     * @param surname user's surname
     * @param email user's email
     * @param password user's password
     * @param confirmPassword password confirmation
     * @param role user's role
     * @return the created {@link User} entity
     * @throws IllegalArgumentException if validation fails or email exists
     */
    public User registerUser(String firstName, String surname, String email, 
                             String password, String confirmPassword, String role) {
        
        validateUserRegistration(firstName, surname, email, password, confirmPassword, role);
        
        if (userRepository.existsByUserEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User();
        user.setUserEmail(email);
        user.setUserName(firstName + " " + surname);
        user.setUserPassword(hashPassword(password)); 
        user.setUserType(role);
        
        return userRepository.save(user);
    }
    
    /**
     * @brief Authenticates a user by email and password.
     * 
     * @param email the user's email
     * @param password the user's password
     * @return the authenticated {@link User}
     * @throws IllegalArgumentException if authentication fails
     */
    public User authenticateUser(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        Optional<User> userOpt = userRepository.findByUserEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        User user = userOpt.get();
        if (!verifyPassword(password, user.getUserPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        if (user.getUserDeactivationStatus()) {
            throw new IllegalArgumentException("Account has been deactivated");
        }
        
        return user;
    }
    
    /**
     * @brief Validates user registration input fields.
     * 
     * @param firstName user's first name
     * @param surname user's surname
     * @param email user's email
     * @param password user's password
     * @param confirmPassword password confirmation
     * @param role user's role
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUserRegistration(String firstName, String surname, String email, 
                                        String password, String confirmPassword, String role) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (surname == null || surname.trim().isEmpty()) {
            throw new IllegalArgumentException("Surname is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select a role");
        }
        if (!role.equals("Student") && !role.equals("Club_Organiser") && !role.equals("Administrator")) {
            throw new IllegalArgumentException("Invalid role selected");
        }
    }

    /**
     * @brief Hashes a password using SHA-256 with a salt.
     * 
     * @param password the password to hash
     * @return hashed password as a Base64 string
     */
    private String hashPassword(String password) {
        try {
            String salt = "P0402";
            String tempPassword = salt + password;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(tempPassword.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * @brief Verifies a password against a stored hashed password.
     * 
     * @param inputPassword the password to verify
     * @param storedHashedPassword stored hashed password
     * @return true if password matches, false otherwise
     */
    private boolean verifyPassword(String inputPassword, String storedHashedPassword) {
        String hashedInput = hashPassword(inputPassword);
        return hashedInput.equals(storedHashedPassword);
    }

    /**
     * @brief Creates a Base64-encoded auth token from email and password.
     * 
     * @param email user's email
     * @param password user's password
     * @return Base64-encoded auth token
     */
    public String createAuthToken(String email, String password) {
        String tokenStr = email + ":" + password;
        return Base64.getEncoder().encodeToString(tokenStr.getBytes());
    }

    /**
     * @brief Decodes a Base64-encoded auth token into email and password.
     * 
     * @param authToken the auth token
     * @return array with email at index 0 and password at index 1
     */
    public String[] decodeAuthToken(String authToken) {
        byte[] decodedBytes = Base64.getDecoder().decode(authToken);
        String decodedStr = new String(decodedBytes);
        return decodedStr.split(":", 2);
    }

    /**
     * @brief Verifies if the auth token is valid and user is active.
     * 
     * @param authToken the auth token
     * @return true if valid, false otherwise
     */
    public boolean verifyLogin(String authToken) {
        try {
            String[] credentials = decodeAuthToken(authToken);
            Optional<User> user = userRepository.findByUserEmail(credentials[0]);
            if (user.isEmpty()) {
                return false;
            }
            User authUser = user.get();
            return verifyPassword(credentials[1], authUser.getUserPassword()) && !authUser.getUserDeactivationStatus();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @brief Retrieves a user from auth token.
     * 
     * @param authToken the auth token
     * @return the {@link User} entity
     * @throws IllegalArgumentException if invalid token or user deactivated
     */
    public User getUserByAuthToken(String authToken) {
        try {
            String[] credentials = decodeAuthToken(authToken);
            Optional<User> userOpt = userRepository.findByUserEmail(credentials[0]);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("Invalid auth token");
            }
            User user = userOpt.get();
            if (!verifyPassword(credentials[1], user.getUserPassword()) || user.getUserDeactivationStatus()) {
                throw new IllegalArgumentException("Invalid auth token or account deactivated");
            }
            return user;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid auth token", e);
        }
    }

    /**
     * @brief Retrieves all users sorted alphabetically by name.
     * 
     * @return list of all {@link User} entities
     */
    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Order.by("userName").ignoreCase()));
    }

    /**
     * @brief Retrieves a user by email.
     * 
     * @param userEmail user's email
     * @return the {@link User} entity
     */
    @Transactional
    public User getUserByEmail(String userEmail) {
        return userRepository.getByUserEmail(userEmail);
    }

    /**
     * @brief Deletes a user by email.
     * 
     * @param userEmail user's email
     * @return true if deletion was successful
     */
    @Transactional
    public boolean deleteUserByEmail(String userEmail) {
        int deletedCount = userRepository.deleteByEmail(userEmail);
        return deletedCount > 0;
    }

    /**
     * @brief Edits an existing user by email.
     * 
     * @param userEmail the user's email
     * @param user updated user data
     * @return the updated {@link User} entity
     * @throws IllegalArgumentException if user not found
     */
    public User editUserByEmail(String userEmail, User user) {
        User existingUser = userRepository.getByUserEmail(userEmail);
        if (existingUser == null) {
            throw new IllegalArgumentException("User with email " + userEmail + " not found");
        }

        existingUser.setUserName(user.getUserName());
        existingUser.setUserPassword(user.getUserPassword());
        existingUser.setUserType(user.getUserType());
        existingUser.setUserDeactivationStatus(user.getUserDeactivationStatus());
        existingUser.setUserEventCounter(user.getUserEventCounter());

        return userRepository.save(existingUser);
    }

    /**
     * @brief Searches users by query string.
     * 
     * If query is null or empty, returns all users.
     * 
     * @param query search string
     * @return list of {@link User} entities
     */
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(query);
    }
}