package au.edu.rmit.sept.webapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * @brief Represents a user in the system.
 * This entity stores user details including email, name, password,
 * user type, deactivation status, event counter, and join date.
 * 
 * @author Agampreet Singh
 * @version 1.0
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Email address of the user (Primary Key).
     */
    @Id
    @Column(length = 255)
    private String userEmail;

    /**
     * Name of the user.
     */
    @Column(length = 100, nullable = false)
    private String userName;

    /**
     * Encrypted password of the user.
     */
    @Column(length = 255, nullable = false)
    private String userPassword;

    /**
     * Type of user (e.g., Student, Club Organiser, Administrator).
     */
    @Column(length = 50, nullable = false)
    private String userType;

    /**
     * Indicates whether the user account is deactivated.
     */
    @Column(nullable = false)
    private Boolean userDeactivationStatus = false;

    /**
     * Counter tracking the number of events associated with the user.
     */
    @Column(nullable = false)
    private Integer userEventCounter = 0;

    /**
     * Date and time when the user joined the system.
     */
    @Column(nullable = false)
    private LocalDateTime userJoinDate = LocalDateTime.now();

    // form binding fields
    @Transient
    private String firstName;
    @Transient
    private String surname;
    @Transient
    private String email;
    @Transient
    private String password;
    @Transient
    private String confirmPassword;
    @Transient
    private String role;

    // Constructors

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * Constructor with essential fields.
     * 
     * @param userEmail the user's email address
     * @param userName the user's name
     * @param userPassword the user's password
     * @param userType the user's type
     */
    public User(String userEmail, String userName, String userPassword, String userType) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userType = userType;
        this.userDeactivationStatus = false;
        this.userEventCounter = 0;
        this.userJoinDate = LocalDateTime.now();
    }

    // Getters and Setters

    /**
     * Returns the user's email address.
     * 
     * @return the user email
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Sets the user's email address.
     * 
     * @param userEmail the user email to set
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Returns the user's name.
     * 
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user's name.
     * 
     * @param userName the user name to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the user's password.
     * 
     * @return the user password
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Sets the user's password.
     * 
     * @param userPassword the user password to set
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Returns the user's type.
     * 
     * @return the user type
     */
    public String getUserType() {
        return userType;
    }

    /**
     * Sets the user's type.
     * 
     * @param userType the user type to set
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * Returns whether the user account is deactivated.
     * 
     * @return the user deactivation status
     */
    public Boolean getUserDeactivationStatus() {
        return userDeactivationStatus;
    }

    /**
     * Sets the user's deactivation status.
     * 
     * @param userDeactivationStatus the deactivation status to set
     */
    public void setUserDeactivationStatus(Boolean userDeactivationStatus) {
        this.userDeactivationStatus = userDeactivationStatus;
    }

    /**
     * Returns the user's event counter.
     * 
     * @return the user event counter
     */
    public Integer getUserEventCounter() {
        return userEventCounter;
    }

    /**
     * Sets the user's event counter.
     * 
     * @param userEventCounter the event counter to set
     */
    public void setUserEventCounter(Integer userEventCounter) {
        this.userEventCounter = userEventCounter;
    }

    /**
     * Returns the user's join date.
     * 
     * @return the user join date
     */
    public LocalDateTime getUserJoinDate() {
        return userJoinDate;
    }

    /**
     * Sets the user's join date.
     * 
     * @param userJoinDate the join date to set
     */
    public void setUserJoinDate(LocalDateTime userJoinDate) {
        this.userJoinDate = userJoinDate;
    }
    // Form binding getters and setters
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }
    public String getEmail() {
        return email != null ? email : userEmail; // Return form email or database email
    }
    public void setEmail(String email) {
        this.email = email;
        this.userEmail = email; // Also set the database field
    }
    public String getPassword() {
        return password != null ? password : userPassword; // Return form password or database password
    }
    public void setPassword(String password) {
        this.password = password;
        this.userPassword = password; // Also set the database field
    }
    public String getConfirmPassword() {
        return confirmPassword;
    }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    public String getRole() {
        return role != null ? role : userType; // Return form role or database userType
    }
    public void setRole(String role) {
        this.role = role;
        this.userType = role; // Also set the database field
    }
    
}
