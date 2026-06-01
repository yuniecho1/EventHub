package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Photo;
import au.edu.rmit.sept.webapp.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @brief Service class for managing photo operations
 * @author Your Name
 * @version 1.0
 */
@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private EventService eventService;

    /**
     * @brief Adds a photo to the Photos table
     * Saves the uploaded file's binary data to the database associated with an event
     * 
     * @param file the uploaded MultipartFile containing the image
     * @param eventId the ID of the event to associate the photo with
     * @return the saved Photo entity with generated ID
     * @throws IOException if there's an error reading the file data
     * @throws IllegalArgumentException if the event doesn't exist
     */
    public Photo addPhotoToEvent(MultipartFile file, Long eventId) throws IOException {
        // Validate that the event exists
        Event event = eventService.getEventById(eventId);
        
        if (event == null) {
            throw new IllegalArgumentException("Event not found with ID: " + eventId);
        }

        // Create new Photo entity
        Photo photo = new Photo();
        photo.setEventId(event);
        photo.setImageData(file.getBytes());

        // Save to database and return
        return photoRepository.save(photo);
    }

    /**
     * @brief Get all photos for a specific event
     * @param eventId the event ID
     * @return list of photos associated with the event
     */
    public List<Photo> getPhotosByEventId(Long eventId) {
        return photoRepository.findByEventId_EventId(eventId);
    }

    /**
     * @brief Get a specific photo by its ID
     * @param photoId the photo ID
     * @return the Photo entity or null if not found
     */
    public Photo getPhotoById(Long photoId) {
        return photoRepository.findById(photoId).orElse(null);
    }

    /**
     * @brief Delete a photo by its ID
     * @param photoId the photo ID to delete
     * @return true if deleted successfully, false if photo not found
     */
    public boolean deletePhoto(Long photoId) {
        if (photoRepository.existsById(photoId)) {
            photoRepository.deleteById(photoId);
            return true;
        }
        return false;
    }

    /**
     * @brief Count total photos for an event
     * @param eventId the event ID
     * @return number of photos
     */
    public long countPhotosByEventId(Long eventId) {
        return photoRepository.findByEventId_EventId(eventId).size();
    }
}