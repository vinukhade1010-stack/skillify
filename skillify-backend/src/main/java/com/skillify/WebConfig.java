package com.skillify;

import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

/** CORS for the frontend + login check on every /api route except /api/auth. */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired UserRepo users;
    @Value("${cors.origins}") String[] origins;

    @Override public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/api/**").allowedOrigins(origins).allowedMethods("GET", "POST", "PUT", "OPTIONS").allowedHeaders("*");
    }

    @Override public void addInterceptors(InterceptorRegistry r) {
        r.addInterceptor(new HandlerInterceptor() {
            @Override public boolean preHandle(HttpServletRequest q, HttpServletResponse s, Object h) {
                if (q.getMethod().equals("OPTIONS")) return true;
                String a = q.getHeader("Authorization");
                Optional<User> u = (a != null && a.startsWith("Bearer ")) ? users.findByToken(a.substring(7)) : Optional.empty();
                if (u.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in again");
                q.setAttribute("user", u.get());
                return true;
            }
        }).addPathPatterns("/api/**").excludePathPatterns("/api/auth/**");
    }
}
