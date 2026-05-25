package com.example.travel_backend.config;

import com.example.travel_backend.entity.User;
import com.example.travel_backend.entity.UserSetting;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.repository.UserSettingsRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class UserSyncFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            UUID userId = UUID.fromString(jwt.getSubject());

            if (!userRepository.existsById(userId)) {
                System.out.println("UserSyncFilter: Auto-syncing missing user ID " + userId);
                
                String email = jwt.getClaimAsString("email");
                String name = "Traveler";
                
                Map<String, Object> userMetadata = jwt.getClaim("user_metadata");
                if (userMetadata != null) {
                    if (userMetadata.containsKey("name")) {
                        name = (String) userMetadata.get("name");
                    } else if (userMetadata.containsKey("full_name")) {
                        name = (String) userMetadata.get("full_name");
                    }
                }

                User newUser = new User();
                newUser.setId(userId);
                newUser.setEmail(email != null ? email : "");
                newUser.setName(name != null ? name : "Traveler");
                newUser.setCreatedAt(OffsetDateTime.now());
                newUser.setIsVerified(true);
                newUser.setExplorerLevel("newbie");
                newUser.setTotalProvinces(0);
                newUser.setFollowerCount(0);
                newUser.setFollowingCount(0);
                newUser.setPostCount(0);
                newUser.setIsPrivate(false);
                userRepository.save(newUser);

                UserSetting newSettings = new UserSetting();
                newSettings.setUser(newUser);
                newSettings.setLanguage("vi");
                newSettings.setTheme("light");
                newSettings.setPushReactions(true);
                newSettings.setPushComments(true);
                newSettings.setPushFollows(true);
                newSettings.setPushReposts(true);
                newSettings.setPushMentions(true);
                newSettings.setPushAchievements(true);
                userSettingsRepository.save(newSettings);
            }
        }

        filterChain.doFilter(request, response);
    }
}
