package au.edu.rmit.sept.webapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import au.edu.rmit.sept.webapp.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor to check for authentication on protected routes.
 * Verifies the presence and validity of an auth token cookie.
 * Redirects to the sign-in page if authentication fails.
 * 
 * @author Agampreet Singh
 * @version 1.0
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    /**
     * @brief Checks authentication before handling a request.
     * 
     * Looks for a cookie named "authToken", verifies it, and attaches
     * the authenticated {@link User} to the request. Redirects to sign-in
     * if the token is missing or invalid. Also checks admin page access.
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response to potentially redirect
     * @param handler the handler chosen to execute
     * @return true if request processing should continue, false otherwise
     * @throws Exception if any error occurs during authentication
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    authToken = cookie.getValue();
                    break;
                }
            }
        }

        if (authToken != null && userService.verifyLogin(authToken)) {
            User user = userService.getUserByAuthToken(authToken);
            request.setAttribute("authenticatedUser", user);

            // Check for admin-only pages and validate user type
            String path = request.getRequestURI();
            if (path.startsWith("/admin") && !"administrator".equalsIgnoreCase(user.getUserType())) {
                response.sendRedirect("/sign-in");
                return false;
            }

            return true; 
        } else {
            response.sendRedirect("/sign-in"); 
            return false; 
        }
    }    
}
