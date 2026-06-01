package au.edu.rmit.sept.webapp.Club;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import au.edu.rmit.sept.webapp.model.Club;

/**
 * @brief Helper class to create {@link Club} instances for testing purposes.
 */
public class ClubHelperTest {

    /**
     * Creates a test {@link Club} object with the given parameters.
     *
     * @param name the club name
     * @param description the club description
     * @param tag the club tag
     * @return a fully constructed {@link Club} object
     */
    public static Club createClub(String name, String description, String tag) {
        Club club = new Club();
        club.setClubName(name);
        club.setClubDescription(description);
        club.setClubTag(tag);
        club.setClubCreationDate(LocalDateTime.now());
        return club;
    }

    /**
     * Creates a test club with a specific ID (for testing purposes).
     * Note: The ID will be overridden when saved to the database.
     * 
     * @param clubId the club ID
     * @param name the club name
     * @param description the club description
     * @param tag the club tag
     * @return a {@link Club} object with the specified ID
     */
    public static Club createClubWithId(Long clubId, String name, String description, String tag) {
        Club club = createClub(name, description, tag);
        club.setClubId(clubId);
        return club;
    }

    /**
     * Creates a default test club with common dummy values.
     * 
     * @param name the club name
     * @return a default {@link Club} with preset dummy fields
     */
    public static Club createDefaultClub(String name) {
        return createClub(name, "Default description for " + name, "general");
    }

    /**
     * Creates a tech-focused club for testing.
     * 
     * @param name the club name
     * @return a {@link Club} with tech-related attributes
     */
    public static Club createTechClub(String name) {
        return createClub(name, "A club for technology enthusiasts and developers", "tech");
    }

    /**
     * Creates a sports-focused club for testing.
     * 
     * @param name the club name
     * @return a {@link Club} with sports-related attributes
     */
    public static Club createSportsClub(String name) {
        return createClub(name, "A club for sports activities and fitness", "sport");
    }

    /**
     * Creates a music-focused club for testing.
     * 
     * @param name the club name
     * @return a {@link Club} with music-related attributes
     */
    public static Club createMusicClub(String name) {
        return createClub(name, "A club for music lovers and performers", "music");
    }

    /**
     * Creates a club with null fields for testing validation.
     * 
     * @param nullField the field to set as null ("name", "description", or "tag")
     * @return a {@link Club} with the specified null field
     */
    public static Club createClubWithNullField(String nullField) {
        Club club = createDefaultClub("Test Club");
        
        switch (nullField.toLowerCase()) {
            case "name" -> club.setClubName(null);
            case "description" -> club.setClubDescription(null);
            case "tag" -> club.setClubTag(null);
        }
        
        return club;
    }

    /**
     * Creates multiple clubs with different tags for testing.
     * 
     * @param baseNamePrefix the prefix for club names
     * @param count the number of clubs to create
     * @return a list of {@link Club} objects with different tags
     */
    public static List<Club> createMultipleClubsWithDifferentTags(String baseNamePrefix, int count) {
        List<Club> clubs = new ArrayList<>();
        String[] tags = {"tech", "sport", "music", "art", "science", "business"};
        
        for (int i = 0; i < count; i++) {
            String tag = tags[i % tags.length];
            String name = baseNamePrefix + " " + (i + 1);
            String description = "Description for " + name + " focused on " + tag;
            
            clubs.add(createClub(name, description, tag));
        }
        
        return clubs;
    }

    /**
     * Creates multiple clubs with the same tag for testing.
     * 
     * @param baseNamePrefix the prefix for club names
     * @param tag the common tag for all clubs
     * @param count the number of clubs to create
     * @return a list of {@link Club} objects with the same tag
     */
    public static List<Club> createMultipleClubsWithSameTag(String baseNamePrefix, String tag, int count) {
        List<Club> clubs = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            String name = baseNamePrefix + " " + i;
            String description = "Description for " + name;
            clubs.add(createClub(name, description, tag));
        }
        
        return clubs;
    }

    /**
     * Creates a club with a specific creation date for testing time-based operations.
     * 
     * @param name the club name
     * @param description the club description
     * @param tag the club tag
     * @param creationDate the specific creation date
     * @return a {@link Club} with the specified creation date
     */
    public static Club createClubWithSpecificDate(String name, String description, String tag, LocalDateTime creationDate) {
        Club club = createClub(name, description, tag);
        club.setClubCreationDate(creationDate);
        return club;
    }

    /**
     * Creates a club with empty strings (but not null) for testing validation.
     * 
     * @return a {@link Club} with empty string fields
     */
    public static Club createClubWithEmptyFields() {
        Club club = new Club();
        club.setClubName("");
        club.setClubDescription("");
        club.setClubTag("");
        club.setClubCreationDate(LocalDateTime.now());
        return club;
    }

    /**
     * Creates a club with very long field values for testing field length constraints.
     * 
     * @return a {@link Club} with long field values
     */
    public static Club createClubWithLongFields() {
        String longName = "A".repeat(300); // Very long name
        String longDescription = "B".repeat(1000); // Very long description
        String longTag = "C".repeat(100); // Very long tag
        
        return createClub(longName, longDescription, longTag);
    }

    /**
     * Creates a club with special characters in the fields for testing character handling.
     * 
     * @return a {@link Club} with special characters
     */
    public static Club createClubWithSpecialCharacters() {
        return createClub(
            "Club with Special Characters & Symbols!",
            "A club that tests special characters: @#$%^&*()_+-={}[]|\\:;\"'<>?,./",
            "special-chars"
        );
    }
}