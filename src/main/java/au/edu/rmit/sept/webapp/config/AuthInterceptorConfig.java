package au.edu.rmit.sept.webapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import au.edu.rmit.sept.webapp.service.AuthInterceptor;


/**
 * Configuration class to register the AuthInterceptor for protected routes.
 * Ensures that authentication checks are applied to specified URL patterns.
 * 
 * @author Agampreet Singh
 * @version 1.0
 */
@Configuration
public class AuthInterceptorConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/event/**", "/dashboard", "/search", "/search-results", "/admin-dashboard", "/user/**", "/club/**") // Protected routes
                .excludePathPatterns("/sign-in", "/sign-up", "/css/**", "/js/**"); // Public routes
    }
}