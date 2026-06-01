package au.edu.rmit.sept.webapp.controller;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import au.edu.rmit.sept.webapp.model.Club;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.ClubService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ClubController {
    @Autowired
    private ClubService clubService;

    /**
     * @brief Displays the form for creating a new club.
     *
     * @param model the model to bind attributes to the view
     * @return the name of the Thymeleaf template for the club creation form
     */
    @GetMapping("/club/form")
    public String showClubForm(Model model) {
        model.addAttribute("club", new Club());
        return "club-form";
    }

    
    /**
     * @brief Retrieves and displays a list of clubs based on search, filtering, and sorting parameters.
     * 
     * If the authenticated user is a club organiser, only their clubs are displayed.
     * Clubs can be filtered by tag or organiser, and sorted by name, tag, or creation date.
     *
     * @param query the optional search query string for club name, description, or tag
     * @param sort the optional sorting criteria ("name", "tag", or "dateCreated")
     * @param filteredTag the optional tag filter
     * @param filteredOrganiser the optional organiser filter
     * @param model the model to bind attributes for the view
     * @param request the HTTP request used to retrieve the authenticated user
     * @return the name of the Thymeleaf template displaying the list of clubs
     */
    @GetMapping("/club/all")
    public String handleClubRetrieval(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "filteredTag", required = false) String filteredTag,
            @RequestParam(value = "filteredOrganiser", required = false) String filteredOrganiser,
            Model model,
            HttpServletRequest request) {
        try {
            
        if ("All".equalsIgnoreCase(filteredTag)|| (filteredTag != null && filteredTag.isEmpty())) {
            return "redirect:/club/all";
        }

            //only show personal clubs if user is organiser
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            if (authenticatedUser.getUserType().equalsIgnoreCase("club_organiser")) {
                filteredOrganiser = authenticatedUser.getUserEmail();
            }

            // Search clubs
            List<Club> clubList = clubService.searchAndFilterClubs(query, filteredTag, filteredOrganiser);


            // Default sort by name
            clubList.sort(Comparator.comparing(Club::getClubName, String.CASE_INSENSITIVE_ORDER));

            if (sort != null && !sort.isEmpty()) {
                switch (sort) {
                    case "name" -> clubList.sort(Comparator.comparing(Club::getClubName, String.CASE_INSENSITIVE_ORDER));
                    case "tag" -> clubList.sort(Comparator.comparing(Club::getClubTag, String.CASE_INSENSITIVE_ORDER));
                    case "dateCreated" -> clubList.sort(Comparator.comparing(Club::getClubCreationDate));
                    default -> {}
                }
            }

            // Add attributes for Thymeleaf template
            model.addAttribute("clubList", clubList);
            model.addAttribute("query", query);
            model.addAttribute("sort", sort);
            model.addAttribute("filteredTag", filteredTag);
            model.addAttribute("filteredOrganiser", filteredOrganiser);

            model.addAttribute("userType", authenticatedUser.getUserType());

            // Populate dropdowns for filters
            model.addAttribute("allClubTags", clubService.findAllTags());
            model.addAttribute("allOrganisers", clubService.findAllOrganisers());

            return "club-list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "club-list";
        }
    }


    /**
     * @brief Handles the submission of the club creation form.
     * Attempts to create a new club using the {@link ClubService}. On success,
     * returns a success page with the created club details. On failure,
     * redisplays the form with an error message.
     *
     * @param club the club object bound from the form submission
     * @param model the model to bind attributes to the view
     * @return the name of the view template to render
     */
    @PostMapping("/club/create")
    public String handleClubSubmission(@ModelAttribute Club club, Model model, HttpServletRequest request) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");

            if (authenticatedUser != null) {
                model.addAttribute("user", authenticatedUser);
            }

            clubService.createClub(club, authenticatedUser);

            return "redirect:/club/all";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "club-form";
        }
    }

    /**
     * @brief Redirects to the details page for the selected club.
     *
     * @param clubId the ID of the clicked club
     * @return a redirect string to the club details page
     */
    @PostMapping("/club/all")
    public String handleClubClick(@RequestParam Long clubId) {
        return "redirect:/club/" + clubId;
    }

    /**
     * @brief Retrieves and displays the details of a specific club.
     * Dynamically determines which UI controls to show based on the
     * authenticated user's role (Club Organiser or Administrator).
     *
     * @param clubId the ID of the club to retrieve
     * @param model  the model to bind attributes to the view
     * @param request the HTTP request to obtain the authenticated user
     * @return the name of the club details template
     */
    @GetMapping("/club/{clubId}")
    public String getClubById(@PathVariable Long clubId, Model model, HttpServletRequest request) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");

            model.addAttribute("showEdit", false);
            model.addAttribute("showDelete", false);
            model.addAttribute("showAddEvent", false);

            if (authenticatedUser != null) {
                model.addAttribute("user", authenticatedUser);
                model.addAttribute("userRole", authenticatedUser.getUserType());

                String userType = authenticatedUser.getUserType();
                if ("Club_Organiser".equals(userType)) {
                    // Only the organiser of this club can edit/delete
                    Club club = clubService.getClubById(clubId);
                    if (club.getUserEmail().equals(authenticatedUser.getUserEmail())) {
                        model.addAttribute("showEdit", true);
                        model.addAttribute("showDelete", true);
                        model.addAttribute("showAddEvent", true);
                    }
                } else if ("Administrator".equals(userType)) {
                    model.addAttribute("showEdit", true);
                    model.addAttribute("showDelete", true);
                }
            }

            Club club = clubService.getClubById(clubId);
            model.addAttribute("club", club);

            return "club-details";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "club-details";
        }
    }
}