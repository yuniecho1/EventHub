package au.edu.rmit.sept.webapp.Photo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Photo;
import au.edu.rmit.sept.webapp.repository.ClubRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.PhotoRepository;

/**
 * @brief Integration test class for {@link PhotoRepository}.
 *        Tests CRUD operations and custom query methods for the Photo entity.
 * 
 * @author Your Name
 * @version 1.0
 */
@SpringBootTest
@Transactional
public class PhotoRepositoryTest {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ClubRepository clubRepository;

    private Event testEvent;
    private Club testClub;
    private Photo testPhoto;

    @BeforeEach
    public void setup() {
        // Clear the database
        photoRepository.deleteAll();
        eventRepository.deleteAll();
        clubRepository.deleteAll();

        // Setup test club
        testClub = new Club();
        testClub.setClubName("Test Club");
        testClub = clubRepository.save(testClub);

        // Setup test event
        testEvent = new Event();
        testClub.setClubName("Test Club");
        testClub = clubRepository.save(testClub);

        // Setup test event
        testEvent = new Event();
        testEvent.setClub(testClub);
        testEvent.setEventTitle("Test Event");
        testEvent.setEventDes("Test Description");
        testEvent.setEventDate(LocalDate.now());
        testEvent.setEventTag("test");
        testEvent.setLocation("Test Location");
        testEvent.setPrice("Free");
        testEvent = eventRepository.save(testEvent);

        // Setup test photo
        testPhoto = new Photo();
        testPhoto.setEventId(testEvent);
        testPhoto.setImageData("test image content".getBytes());
        testPhoto = photoRepository.save(testPhoto);
    }

    /**
     * @brief Tests saving a photo to the repository.
     */
    @Test
    void savePhoto_shouldPersistPhoto_whenValidPhotoProvided() {
        Photo newPhoto = new Photo();
        newPhoto.setEventId(testEvent);
        newPhoto.setImageData("new image content".getBytes());

        Photo savedPhoto = photoRepository.save(newPhoto);

        assertThat(savedPhoto).isNotNull();
        assertThat(savedPhoto.getPhotoId()).isNotNull();
        assertThat(savedPhoto.getEventId().getEventId()).isEqualTo(testEvent.getEventId());
        assertThat(savedPhoto.getImageData()).isEqualTo("new image content".getBytes());
    }

    /**
     * @brief Tests retrieving a photo by its ID.
     */
    @Test
    void findById_shouldReturnPhoto_whenPhotoExists() {
        Optional<Photo> foundPhoto = photoRepository.findById(testPhoto.getPhotoId());

        assertThat(foundPhoto).isPresent();
        assertThat(foundPhoto.get().getPhotoId()).isEqualTo(testPhoto.getPhotoId());
        assertThat(foundPhoto.get().getEventId().getEventId()).isEqualTo(testEvent.getEventId());
        assertThat(foundPhoto.get().getImageData()).isEqualTo(testPhoto.getImageData());
    }

    /**
     * @brief Tests retrieving a non-existent photo by ID.
     */
    @Test
    void findById_shouldReturnEmpty_whenPhotoDoesNotExist() {
        Optional<Photo> foundPhoto = photoRepository.findById(999L);

        assertThat(foundPhoto).isNotPresent();
    }

    /**
     * @brief Tests retrieving all photos for a specific event.
     */
    @Test
    void findByEventId_EventId_shouldReturnPhotos_whenEventHasPhotos() {
        // Save a second photo for the same event
        Photo secondPhoto = new Photo();
        secondPhoto.setEventId(testEvent);
        secondPhoto.setImageData("second image content".getBytes());
        photoRepository.save(secondPhoto);

        List<Photo> photos = photoRepository.findByEventId_EventId(testEvent.getEventId());

        assertThat(photos).hasSize(2);
        assertThat(photos).extracting(Photo::getEventId).extracting(Event::getEventId).containsOnly(testEvent.getEventId());
    }

    /**
     * @brief Tests retrieving photos for an event with no photos.
     */
    @Test
    void findByEventId_EventId_shouldReturnEmptyList_whenEventHasNoPhotos() {
        // Create a new event with no photos
        Event newEvent = new Event();
        newEvent.setClub(testClub);
        newEvent.setEventTitle("New Event");
        newEvent.setEventDes("New Description");
        newEvent.setEventDate(LocalDate.now());
        newEvent.setEventTag("test");
        newEvent.setLocation("New Location");
        newEvent.setPrice("Free");
        newEvent = eventRepository.save(newEvent);

        List<Photo> photos = photoRepository.findByEventId_EventId(newEvent.getEventId());

        assertThat(photos).isEmpty();
    }

    /**
     * @brief Tests retrieving photos for a non-existent event.
     */
    @Test
    void findByEventId_EventId_shouldReturnEmptyList_whenEventDoesNotExist() {
        List<Photo> photos = photoRepository.findByEventId_EventId(999L);

        assertThat(photos).isEmpty();
    }

    /**
     * @brief Tests deleting a photo by its ID.
     */
    @Test
    void deleteById_shouldRemovePhoto_whenPhotoExists() {
        photoRepository.deleteById(testPhoto.getPhotoId());

        assertThat(photoRepository.existsById(testPhoto.getPhotoId())).isFalse();
    }

    /**
     * @brief Tests deleting a non-existent photo.
     */
    @Test
    void deleteById_shouldNotThrowException_whenPhotoDoesNotExist() {
        photoRepository.deleteById(999L);

        assertThat(photoRepository.count()).isEqualTo(1); // Only testPhoto remains
    }

    /**
     * @brief Tests counting photos in the repository.
     */
    @Test
    void count_shouldReturnCorrectCount_whenPhotosExist() {
        // Save an additional photo
        Photo anotherPhoto = new Photo();
        anotherPhoto.setEventId(testEvent);
        anotherPhoto.setImageData("another image content".getBytes());
        photoRepository.save(anotherPhoto);

        long count = photoRepository.count();

        assertThat(count).isEqualTo(2);
    }

    /**
     * @brief Tests counting photos when none exist.
     */
    @Test
    void count_shouldReturnZero_whenNoPhotosExist() {
        photoRepository.deleteAll();

        long count = photoRepository.count();

        assertThat(count).isEqualTo(0);
    }
}