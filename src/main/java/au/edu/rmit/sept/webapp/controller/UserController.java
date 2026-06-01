package au.edu.rmit.sept.webapp.controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @brief Controller responsible for handling HTTP requests related to {@link User} entities.
 * Provides endpoints for retrieving, viewing, deleting, and banning users.
 * Coordinates between the view layer and the {@link UserService} to manage user data.
 * 
 * This controller enforces access control rules based on the authenticated user's role.
 * Only administrators are permitted to delete or ban users.
 * 
 * @author  Agampreet Singh
 * @version 1.0
 */
@Controller
public class UserController {
    
    @Autowired
    private UserService userService;

    /**
     * @brief Retrieves and displays a list of users.
     * Optionally filters results if a search query is provided.
     *
     * @param query   optional search string to filter users
     * @param model   the model to bind attributes to the view
     * @param request the HTTP request object
     * @return the name of the user list template
     */
    @GetMapping("/user/all")
    public String handleUserRetrieval(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "filteredType", required = false) String filteredType,
            Model model,
            HttpServletRequest request) {
        try {
            List<User> userList = userService.searchUsers(query);

            // Filter by userType if selected
            if (filteredType != null && !filteredType.isEmpty()) {
                userList = userList.stream()
                        .filter(u -> u.getUserType().equalsIgnoreCase(filteredType))
                        .collect(Collectors.toList());
            }

            //Automatically sort by name
            userList.sort(Comparator.comparing(User::getUserName, String.CASE_INSENSITIVE_ORDER));

            if (sort != null && !sort.isEmpty()) {
                switch (sort) {
                    case "email" -> userList.sort(Comparator.comparing(User::getUserEmail, String.CASE_INSENSITIVE_ORDER));
                    case "name" -> userList.sort(Comparator.comparing(User::getUserName, String.CASE_INSENSITIVE_ORDER));
                    case "userType" -> userList.sort(Comparator.comparing(User::getUserType, String.CASE_INSENSITIVE_ORDER));
                    default -> {}
                }
            }

            model.addAttribute("userList", userList);
            model.addAttribute("query", query);
            model.addAttribute("sort", sort);
            model.addAttribute("filteredType", filteredType);

            User authenticatedUser = (User) request.getAttribute("authenticatedUser");

            if (!authenticatedUser.getUserType().equalsIgnoreCase("Administrator")) {
                model.addAttribute("error", "Access denied. You don't have permission to view this page.");
                return "redirect:/dashboard";
            }

            model.addAttribute("userType", authenticatedUser.getUserType());

            return "user-list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "user-list";
        }
    }

    /**
     * @brief Redirects to the details page for the selected user.
     *
     * @param userEmail the email of the user whose details should be displayed
     * @return a redirect string to the user details page
     */
    @PostMapping("/user/all")
    public String handleUserClick(@RequestParam String userEmail) {
        return "redirect:/user/details?email=" + userEmail;
    }

    /**
     * @brief Retrieves and displays detailed information for a specific user.
     * Shows delete controls only if the authenticated user is an administrator.
     *
     * @param userEmail the email address of the user to retrieve
     * @param model     the model to bind attributes to the view
     * @param request   the HTTP request to obtain the authenticated user
     * @return the name of the user details template
     */
    @GetMapping("/user/details")
    public String getUserByEmail(@RequestParam("email") String userEmail, Model model, HttpServletRequest request) {
        try {
            User detailedUser = userService.getUserByEmail(userEmail);
            model.addAttribute("detailedUser", detailedUser);

            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            if (authenticatedUser != null) {
                if (!authenticatedUser.getUserType().equalsIgnoreCase("Administrator")) {
                    model.addAttribute("error", "Access denied. You don't have permission to view this page.");
                    return "redirect:/dashboard";
                }

                model.addAttribute("userRole", authenticatedUser.getUserType());
                boolean showDelete = authenticatedUser.getUserType().equals("Administrator") && !authenticatedUser.getUserEmail().equals(detailedUser.getUserEmail()) && !detailedUser.getUserEmail().equals("super@admin.com");
                model.addAttribute("showDelete", showDelete);     
            }
            else {
                model.addAttribute("userRole", "");
                model.addAttribute("showDelete", false);
            }
            return "user-details";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "user-details";
        }
    }

    /**
     * @brief Shows a confirmation page for deleting a specific user.
     * Only accessible to administrators.
     *
     * @param userEmail the email address of the user to delete
     * @param model     the model to bind attributes to the view
     * @param request   the HTTP request to obtain the authenticated user
     * @return the name of the user delete confirmation template
     */
    @GetMapping("/user/delete")
    public String showDeleteConfirmation(@RequestParam("email") String userEmail, Model model, HttpServletRequest request) {
        try {

            if (userEmail.equals("super@admin.com")) {
                model.addAttribute("error", "Cannot delete the super admin.");
                return "redirect:/user/all";
            }

            User authenticatedUser = (User) request.getAttribute("authenticatedUser");

            if (authenticatedUser == null) {
                model.addAttribute("error", "You must be logged in to delete users.");
                return "redirect:/sign-in";
            }
            String userType = authenticatedUser.getUserType();
            if (!"Administrator".equals(userType)) {
                model.addAttribute("error", "Access denied. You don't have permission to delete users.");
                return "redirect:/user/details?email=" + userEmail;
            }
            
            User detailedUser = userService.getUserByEmail(userEmail);
            model.addAttribute("detailedUser", detailedUser);
            model.addAttribute("userRole", userType);
            return "user-delete";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/user/all";
        }
    }

    /**
     * @brief Handles the deletion of a specific user after confirmation.
     * Only accessible to administrators.
     *
     * @param userEmail the email address of the user to delete
     * @param model     the model to bind attributes to the view
     * @param request   the HTTP request to obtain the authenticated user
     * @return redirect to the user list or details page based on outcome
     */
    @PostMapping("/user/delete")
    public String deleteUser(@RequestParam("email") String userEmail, Model model, HttpServletRequest request) {
        try {

            if (userEmail.equals("super@admin.com")) {
                model.addAttribute("error", "Cannot delete the super admin.");
                return "redirect:/user/details?email=" + userEmail;
            }

            User authenticatedUser = (User) request.getAttribute("authenticatedUser");

            if (authenticatedUser == null) {
                model.addAttribute("error", "You must be logged in to delete users.");
                return "redirect:/sign-in";
            }

            String userType = authenticatedUser.getUserType();
            
            if (!"Administrator".equals(userType)) {
                model.addAttribute("error", "Access denied. You don't have permission to delete users.");
                return "redirect:/user/details?email=" + userEmail;
            }
            
            boolean deleted = userService.deleteUserByEmail(userEmail);
            
            if (deleted) {
                model.addAttribute("success", "User deleted successfully");
                return "redirect:/user/all";
            } else {
                model.addAttribute("error", "User not found or could not be deleted");
                return "redirect:/user/details?email=" + userEmail;
            }
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete user: " + e.getMessage());
            return "redirect:/user/details?email=" + userEmail;
        }
    }

    /**
     * @brief Toggles the ban status (activation/deactivation) of a specific user.
     * Only accessible to administrators. Redirects back to the user's details page.
     *
     * @param userEmail the email address of the user whose ban status will be toggled
     * @param model     the model to bind attributes to the view
     * @param request   the HTTP request to obtain the authenticated user
     * @return redirect to the user details page
     */
    @GetMapping("/user/ban")
    public String toggleBanUser(@RequestParam("email") String userEmail,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        try {

            if (userEmail.equals("super@admin.com")) {
                redirectAttributes.addFlashAttribute("error", "Cannot ban the super admin.");
                return "redirect:/user/details?email=" + userEmail;
            }

            User authenticatedUser = (User) request.getAttribute("authenticatedUser");

            if (authenticatedUser == null) {
                redirectAttributes.addFlashAttribute("error", "You must be logged in to ban users.");
                return "redirect:/sign-in";
            }

            if (!"Administrator".equalsIgnoreCase(authenticatedUser.getUserType())) {
                redirectAttributes.addFlashAttribute("error", "Access denied. You don't have permission to ban users.");
                return "redirect:/user/details?email=" + userEmail;
            }

            User targetUser = userService.getUserByEmail(userEmail);
            if (targetUser == null) {
                redirectAttributes.addFlashAttribute("error", "User not found.");
                return "redirect:/user/all";
            }

            // Toggle ban status
            boolean currentlyDeactivated = targetUser.getUserDeactivationStatus();
            targetUser.setUserDeactivationStatus(!currentlyDeactivated);
            userService.editUserByEmail(userEmail, targetUser);

            redirectAttributes.addFlashAttribute("success", currentlyDeactivated ? "User unbanned successfully." : "User banned successfully.");
            return "redirect:/user/details?email=" + userEmail;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user status: " + e.getMessage());
            return "redirect:/user/details?email=" + userEmail;
        }
    }

    /**
     * @brief Updates the role (userType) of a specific user.
     * Only accessible to administrators.
     *
     * @param userEmail the email of the user whose role will be updated
     * @param newUserType the new role to assign (student, club_organiser, administrator)
     * @param model     the model to bind attributes to the view
     * @param request   the HTTP request to obtain the authenticated user
     * @return redirect to the user's details page or user list
     */
    @PostMapping("/user/updateRole")
    public String updateUserRole(
            @RequestParam("email") String userEmail,
            @RequestParam("userType") String newUserType,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            
            if (userEmail.equals("super@admin.com")) {
                redirectAttributes.addFlashAttribute("error", "Cannot change the role of the super admin.");
                return "redirect:/user/details?email=" + userEmail;
            }

            User authenticatedUser = (User) request.getAttribute("authenticatedUser");

            if (authenticatedUser == null) {
                redirectAttributes.addFlashAttribute("error", "You must be logged in to update user roles.");
                return "redirect:/sign-in";
            }

            if (!"Administrator".equalsIgnoreCase(authenticatedUser.getUserType())) {
                redirectAttributes.addFlashAttribute("error", "Access denied. You don't have permission to update roles.");
                return "redirect:/user/details?email=" + userEmail;
            }

            User targetUser = userService.getUserByEmail(userEmail);
            if (targetUser == null) {
                redirectAttributes.addFlashAttribute("error", "User not found.");
                return "redirect:/user/all";
            }

            // Update role
            targetUser.setUserType(newUserType);
            userService.editUserByEmail(userEmail, targetUser);

            redirectAttributes.addFlashAttribute("success", "User role updated successfully.");
            return "redirect:/user/details?email=" + userEmail;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user role: " + e.getMessage());
            return "redirect:/user/details?email=" + userEmail;
        }
    }
}