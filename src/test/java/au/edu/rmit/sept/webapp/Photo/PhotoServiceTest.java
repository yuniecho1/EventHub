package au.edu.rmit.sept.webapp.Photo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Photo;
import au.edu.rmit.sept.webapp.repository.ClubRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.PhotoRepository;
import au.edu.rmit.sept.webapp.service.PhotoService;

/**
 * @brief Integration test class for {@link PhotoService}.
 *        Tests photo creation, retrieval, deletion, and business logic.
 * 
 * @author Your Name
 * @version 1.0
 */
@SpringBootTest
@Transactional
public class PhotoServiceTest {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ClubRepository clubRepository;

    private Event testEvent;
    private MultipartFile mockFile;
    private Club testClub;

    @BeforeEach
    public void setup() throws IOException {
        photoRepository.deleteAll();
        eventRepository.deleteAll();
        clubRepository.deleteAll();

        // Setup test club
        testClub = new Club();
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

        // Setup mock file
        mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
    }

    /**
     * @brief Tests adding a valid photo to an event.
     */
    @Test
    void addPhotoToEvent_shouldSucceed_whenValidFileAndEventProvided() throws IOException {
        Photo savedPhoto = photoService.addPhotoToEvent(mockFile, testEvent.getEventId());

        assertThat(savedPhoto).isNotNull();
        assertThat(savedPhoto.getPhotoId()).isNotNull();
        assertThat(savedPhoto.getEventId().getEventId()).isEqualTo(testEvent.getEventId());
        assertThat(savedPhoto.getImageData()).isEqualTo(mockFile.getBytes());
    }

    /**
     * @brief Tests adding a photo to a non-existent event.
     */
    @Test
    void addPhotoToEvent_shouldThrowException_whenEventDoesNotExist() {
        Long nonExistentEventId = 999L;

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            photoService.addPhotoToEvent(mockFile, nonExistentEventId)
        );

        assertThat(exception.getMessage()).contains("Event not found with ID: " + nonExistentEventId);
    }

    /**
     * @brief Tests retrieving photos by event ID.
     */
    @Test
    void getPhotosByEventId_shouldReturnPhotos_whenEventHasPhotos() throws IOException {
        photoService.addPhotoToEvent(mockFile, testEvent.getEventId());
        photoService.addPhotoToEvent(mockFile, testEvent.getEventId());

        List<Photo> photos = photoService.getPhotosByEventId(testEvent.getEventId());

        assertThat(photos).hasSize(2);
        assertThat(photos.get(0).getEventId().getEventId()).isEqualTo(testEvent.getEventId());
        assertThat(photos.get(1).getEventId().getEventId()).isEqualTo(testEvent.getEventId());
    }

    /**
     * @brief Tests retrieving photos for an event with no photos.
     */
    @Test
    void getPhotosByEventId_shouldReturnEmptyList_whenNoPhotosExist() {
        List<Photo> photos = photoService.getPhotosByEventId(testEvent.getEventId());

        assertThat(photos).isEmpty();
    }

    /**
     * @brief Tests retrieving a photo by its ID.
     */
    @Test
    void getPhotoById_shouldReturnPhoto_whenPhotoExists() throws IOException {
        Photo savedPhoto = photoService.addPhotoToEvent(mockFile, testEvent.getEventId());

        Photo retrievedPhoto = photoService.getPhotoById(savedPhoto.getPhotoId());

        assertThat(retrievedPhoto).isNotNull();
        assertThat(retrievedPhoto.getPhotoId()).isEqualTo(savedPhoto.getPhotoId());
        assertThat(retrievedPhoto.getEventId().getEventId()).isEqualTo(testEvent.getEventId());
        assertThat(retrievedPhoto.getImageData()).isEqualTo(mockFile.getBytes());
    }

    /**
     * @brief Tests retrieving a non-existent photo by ID.
     */
    @Test
    void getPhotoById_shouldReturnNull_whenPhotoDoesNotExist() {
        Photo retrievedPhoto = photoService.getPhotoById(999L);

        assertThat(retrievedPhoto).isNull();
    }

    /**
     * @brief Tests deleting a photo by its ID.
     */
    @Test
    void deletePhoto_shouldReturnTrue_whenPhotoExists() throws IOException {
        Photo savedPhoto = photoService.addPhotoToEvent(mockFile, testEvent.getEventId());

        boolean deleted = photoService.deletePhoto(savedPhoto.getPhotoId());

        assertThat(deleted).isTrue();
        assertThat(photoRepository.existsById(savedPhoto.getPhotoId())).isFalse();
    }

    /**
     * @brief Tests deleting a non-existent photo.
     */
    @Test
    void deletePhoto_shouldReturnFalse_whenPhotoDoesNotExist() {
        boolean deleted = photoService.deletePhoto(999L);

        assertThat(deleted).isFalse();
    }

    /**
     * @brief Tests counting photos for an event.
     */
    @Test
    void countPhotosByEventId_shouldReturnCorrectCount_whenPhotosExist() throws IOException {
        photoService.addPhotoToEvent(mockFile, testEvent.getEventId());
        photoService.addPhotoToEvent(mockFile, testEvent.getEventId());

        long count = photoService.countPhotosByEventId(testEvent.getEventId());

        assertThat(count).isEqualTo(2);
    }

    /**
     * @brief Tests counting photos for an event with no photos.
     */
    @Test
    void countPhotosByEventId_shouldReturnZero_whenNoPhotosExist() {
        long count = photoService.countPhotosByEventId(testEvent.getEventId());

        assertThat(count).isEqualTo(0);
    }

    /**
     * @brief Tests adding multiple photos to different events.
     */
    @Test
    void addPhotoToEvent_shouldHandleMultipleEvents_whenPhotosAdded() throws IOException {
        Club club2 = new Club();
        club2.setClubName("Second Club");
        club2 = clubRepository.save(club2);

        Event event2 = new Event();
        event2.setClub(club2);
        event2.setEventTitle("Second Event");
        event2.setEventDes("Second Description");
        event2.setEventDate(LocalDate.now());
        event2.setEventTag("test");
        event2.setLocation("Second Location");
        event2.setPrice("Free");
        event2 = eventRepository.save(event2);

        Photo photo1 = photoService.addPhotoToEvent(mockFile, testEvent.getEventId());
        Photo photo2 = photoService.addPhotoToEvent(mockFile, event2.getEventId());

        assertThat(photo1.getEventId().getEventId()).isEqualTo(testEvent.getEventId());
        assertThat(photo2.getEventId().getEventId()).isEqualTo(event2.getEventId());
        assertThat(photoRepository.count()).isEqualTo(2);
    }

    /**
     * @brief Tests transactional rollback when adding a photo fails.
     */
    @Test
    void addPhotoToEvent_shouldNotPersist_whenEventValidationFails() {
        long initialCount = photoRepository.count();
        Long nonExistentEventId = 999L;

        assertThrows(IllegalArgumentException.class, () ->
            photoService.addPhotoToEvent(mockFile, nonExistentEventId)
        );

        assertThat(photoRepository.count()).isEqualTo(initialCount);
    }

    /**
     * @brief Tests adding a photo with empty file content.
     */
    @Test
    void addPhotoToEvent_shouldSucceed_whenFileIsEmpty() throws IOException {
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        Photo savedPhoto = photoService.addPhotoToEvent(emptyFile, testEvent.getEventId());

        assertThat(savedPhoto).isNotNull();
        assertThat(savedPhoto.getPhotoId()).isNotNull();
        assertThat(savedPhoto.getImageData()).isEmpty();
        assertThat(savedPhoto.getEventId().getEventId()).isEqualTo(testEvent.getEventId());
    }
}