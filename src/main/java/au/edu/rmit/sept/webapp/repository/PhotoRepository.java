package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    
    /**
     * Find all photos for a specific event
     * @param eventId the event ID
     * @return list of photos
     */
    List<Photo> findByEventId_EventId(Long eventId);
}