package com.example.travel_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserSyncFilter userSyncFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/sync", "/health", "/health/db").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/provinces/**").permitAll()
                        .requestMatchers(new RegexRequestMatcher("^/api/places$", "GET")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/places/**").permitAll()
                        .requestMatchers(new RegexRequestMatcher("^/api/events$", "GET")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/weather/**").permitAll()
                        .requestMatchers(new RegexRequestMatcher("^/api/users/[0-9a-fA-F\\-]{36}$", "GET")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/user/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments_with_author").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/follows/*/followers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/follows/*/following").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/search/trending").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/search/log").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("admin")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterAfter(userSyncFilter, org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
