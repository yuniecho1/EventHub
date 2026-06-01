# RMIT COSC2299 SEPT Major Project

## Group Information

### Group-P04-02

#### Members
- Yunie Cho (s4001725)
- Zac Spongberg (s4096726)
- Agampreet Singh (s4014941)
- Lucas Aponso (s3896348)

---

## How to Run Tests
Run the following command to execute all tests and generate a code coverage report with Jacoco:

'./mvnw clean test jacoco:report'

Notes:
Make sure you have Java and Maven installed. The ./mvnw wrapper allows you to run Maven even if it’s not installed globally.
Test results will be displayed in the console. Detailed test reports can be found in target/surefire-reports/.
The Jacoco code coverage report will be generated in target/site/jacoco/index.html. Open the HTML file in a browser to view coverage metrics for classes, methods, and lines.

## Features

### For Students
- Browse clubs
- Browse events
- RSVP to events
    - Recieve RSVP emails for confirmation
    - Recieve Email reminders for your upcoming RSVP events

- View events and club details
- View upcoming events on dashboard
- Leave Feedback for events attended

### For Club Organisers
- Create and manage clubs
- Organise and publish events
- View Given Feedback for events

### For Admins
- Manage all users and events
- Assign admin roles to other users
- Deactivate/reactivate users
- Ban/unban users
- Dethrone other admins to either Club Organiser or Student
- Cannot delete themselves or the Super Admin
- Super Admin cannot be deleted or deactivated

---

## Technologies Used

- **API:** Spring Boot
- **Frontend:** Thymeleaf
- **Testing:** JUnit
- **CI/CD:** Docker, GitHub Actions
- **Database:** H2 (development), PostgreSQL/MySQL (production)
- **Other:** Maven, Spring Security

---

## Usage

### How to Run

1. Clone the repository:
    ```
    git clone https://github.com/cosc2299-2025/team-project-group-p04-02.git
    ```
2. Build and run the application in the project directory:
    ```
    ./mvnw spring-boot:run
    ```
3. Access the app at [http://localhost:8080](http://localhost:8080)

### How to Create Credentials

- Register via the sign-up page for Student or Club Organiser accounts.
- For Super Admin, use the credentials below (created automatically).

---

## Login Credentials for Super Admin

- **Email:** super@admin.com
- **Password:** SuperAdmin123

> The Super Admin cannot be deleted or deactivated.
> All Admins can bestow the Admin role to other users.
> The Admins cannot delete themselves or the Super Admin.

---

## Links

- [Github Project Board](https://github.com/orgs/cosc2299-2025/projects/15)


