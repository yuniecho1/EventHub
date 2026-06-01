package au.edu.rmit.sept.webapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

/**
 * Represents feedback submitted by users for events.
 * 
 * @author Yunie Cho
 * @version 1.0
 */
@Entity
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private String userEmail;

    @Column(length = 200, nullable = false)
    private String feedbackText; // Max 200 characters

    @Column(nullable = false)
    private Integer rating; // 1-5 rating

    @Column(nullable = false)
    private LocalDateTime submissionDate = LocalDateTime.now();

    // Constructors
    public Feedback() {}

    public Feedback(Long eventId, String userEmail, String feedbackText, Integer rating) {
        this.eventId = eventId;
        this.userEmail = userEmail;
        this.feedbackText = feedbackText;
        this.rating = rating;
    }

    // Getters and Setters
    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }
}