package au.edu.rmit.sept.webapp.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EventRepository eventRepository; 

    @Autowired
    private RSVPRepository rsvpRepository; 

    @Async
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("eventhubrmit@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // @Scheduled(fixedRate = 60000, initialDelay = 120000)  // DEBUG/TEST - 60000 ms = 1 minute, 120000 ms = 2 minutes initial delay
    @Scheduled(cron = "0 0 9 * * ?")  // 9 AM every day
    public void sendReminders() {
        LocalDate tomz = LocalDate.now().plusDays(1);
        List<Event> events = eventRepository.findByEventDate(tomz);

        for (Event event : events) {
            List<RSVP> rsvps = rsvpRepository.findAllByEventId(event.getEventId());
            for (RSVP rsvp : rsvps) {
                String to = rsvp.getUserEmail();
                String subject = "Event Reminder: Your event is tomorrow!";
                String htmlBody = "<div style=\"font-family:Arial,sans-serif;background:#f9f9f9;padding:20px;border-radius:8px;max-width:500px;margin:auto;\">"
                        + "<h2 style=\"color:#0072c6;\">EventHub RMIT Reminder</h2>"
                        + "<p style=\"font-size:16px;\">Hi <strong>" + rsvp.getUserEmail() + "</strong>,</p>"
                        + "<p style=\"font-size:15px;\">This is a reminder that the event <span style=\"color:#0072c6;font-weight:bold;\">" + event.getEventTitle() + "</span> is scheduled for tomorrow (<strong>" + event.getEventDate() + "</strong>).</p>"
                        + "<p style=\"font-size:15px;\"><strong>Location:</strong> " + event.getLocation() + "</p>"
                        + "<hr style=\"border:none;border-top:1px solid #eee;\"/>"
                        + "<p style=\"font-size:15px;\">See you there!</p>"
                        + "<p style=\"font-size:14px;color:#555;\">Regards,<br/>EventHub RMIT</p>"
                        + "</div>";
                sendEmail(to, subject, htmlBody);
            }
        }
    }
}
