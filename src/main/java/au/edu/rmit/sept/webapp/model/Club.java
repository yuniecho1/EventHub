package au.edu.rmit.sept.webapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * Represents a club entity.
 * This entity stores club details including its ID and name,
 * along with the email of a user who made an RSVP.
 * 
 * Note: Club relationship with events or users can be extended 
 * with proper associations in the future.
 * 
 * @author Lucas
 * @version 1.0
 */
@Entity
public class Club {

    /**
     * Unique identifier for the club.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clubId;

    /**
     * Unique identifier for the owner of the club
     */
    private String userEmail;

    /**
     * Name of the club.
     */
    private String clubName;

    /**
     * Description of the club.
     */
    private String clubDescription;

    /**
     * Description of the club.
     */
    private String clubTag;

    /**
     * Date and time when the club was created.
     */
    @Column(nullable = false)
    private LocalDateTime clubCreationDate = LocalDateTime.now();

    // Getters and Setters

    /**
     * Returns the unique ID of the club.
     * 
     * @return the club ID
     */
    public Long getClubId() {
        return clubId;
    }

    /**
     * Sets the unique ID of the club.
     * 
     * @param clubId the club ID to set
     */
    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    /**
     * Returns the owner (userEmail) of the club.
     * 
     * @return the userEmail
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Sets the owner (userEmail) of the club.
     * 
     * @param userEmail the owner to set
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Returns the name of the club.
     * 
     * @return the club name
     */
    public String getClubName() {
        return clubName;
    }

    /**
     * Sets the name of the club.
     * 
     * @param clubName the club name to set
     */
    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    /**
     * Returns the description of the club.
     * 
     * @return the club description
     */
    public String getClubDescription() {
        return clubDescription;
    }

    /**
     * Sets the description of the club.
     * 
     * @param clubName the club description to set
     */
    public void setClubDescription(String clubDescription) {
        this.clubDescription = clubDescription;
    }


    
    
    /**
     * Returns the tag of the club.
     * 
     * @return the club tag
     */
    public String getClubTag() {
        return clubTag;
    }

    /**
     * Sets the tag of the club.
     * 
     * @param clubTag the club tag to set
     */
    public void setClubTag(String clubTag) {
        this.clubTag = clubTag;
    }

    /**
     * Returns the clubs creation date.
     * 
     * @return the clubs creation date
     */
    public LocalDateTime getClubCreationDate() {
        return clubCreationDate;
    }

    /**
     * Sets the clubs creation date.
     * 
     * @param clubCreationDate the join date to set
     */
    public void setClubCreationDate(LocalDateTime clubCreationDate) {
        this.clubCreationDate = clubCreationDate;
    }
}