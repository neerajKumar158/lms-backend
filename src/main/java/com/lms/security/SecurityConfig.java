package com.lms.security;

import com.lms.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Security Configuration - Phase 2.1
 * Role-Based Access Control:
 * - ROLE_STUDENT: Can enroll, view courses, attend sessions
 * - ROLE_TEACHER: Can create courses, upload materials, conduct live sessions
 * - ROLE_ORGANIZATION: Can manage teachers, courses, students
 * - ROLE_ADMIN: System administration
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:}")
    private String allowedOriginsConfig;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserAccountRepository users) {
        return username -> users.findByEmail(username)
                .map(u -> {
                    // Strip "ROLE_" prefix if present, as Spring Security's .roles() adds it automatically
                    String[] roles = u.getRoles() == null ? new String[]{} : 
                        u.getRoles().stream()
                            .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                            .toArray(String[]::new);
                    return User.withUsername(u.getEmail())
                            .password(u.getPasswordHash())
                            .roles(roles)
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Default localhost origins
        List<String> defaultOrigins = List.of(
                "http://localhost:9192",
                "http://localhost:3000",
                "http://localhost:8080"
        );
        
        // Add configurable origins from application properties
        List<String> allOrigins = Stream.concat(
                defaultOrigins.stream(),
                allowedOriginsConfig != null && !allowedOriginsConfig.trim().isEmpty()
                        ? Arrays.stream(allowedOriginsConfig.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                        : Stream.empty()
        ).collect(Collectors.toList());
        
        configuration.setAllowedOrigins(allOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - UI and static resources (MUST BE FIRST)
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/ui/**").permitAll()  // All UI routes are public
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/lms/courses").permitAll() // Public course listing
                        .requestMatchers("/api/lms/courses/free").permitAll()
                        .requestMatchers("/api/lms/courses/search").permitAll()
                        .requestMatchers("/api/lms/courses/{id}").permitAll() // Public course details
                        .requestMatchers("/api/lms/courses/{id}/lectures").permitAll() // Public course lectures
                        .requestMatchers("/api/lms/lectures/{id}/materials").permitAll() // Public lecture materials (for free content)
                        
                        // Profile endpoints (authenticated users)
                        .requestMatchers("/api/profile/**").authenticated()
                        
                        // Student endpoints
                        .requestMatchers("/api/lms/enrollments/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/api/lms/student/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/api/lms/report-card/student").hasAnyRole("STUDENT", "ADMIN")
                        // Allow teachers to view student report cards (controller will verify they're the instructor)
                        .requestMatchers("/api/lms/report-card/student/**").hasAnyRole("STUDENT", "TEACHER", "ORGANIZATION", "ADMIN")
                        
                        // Teacher endpoints
                        .requestMatchers("/api/lms/courses", "POST").hasAnyRole("TEACHER", "ORGANIZATION", "ADMIN")
                        .requestMatchers("/api/lms/courses/{id}", "PUT", "DELETE").hasAnyRole("TEACHER", "ORGANIZATION", "ADMIN")
                        .requestMatchers("/api/lms/courses/{id}/lectures", "POST").hasAnyRole("TEACHER", "ORGANIZATION", "ADMIN")
                        .requestMatchers("/api/lms/teacher/**").hasAnyRole("TEACHER", "ORGANIZATION", "ADMIN")
                        .requestMatchers("/api/lms/upload/**").hasAnyRole("TEACHER", "ORGANIZATION", "ADMIN")
                        .requestMatchers("/api/lms/live-sessions", "POST").hasAnyRole("TEACHER", "ORGANIZATION", "ADMIN")
                        .requestMatchers("/api/lms/report-card/course/**").hasAnyRole("TEACHER", "ORGANIZATION", "ADMIN")
                        .requestMatchers("/api/lms/analytics/**").hasAnyRole("TEACHER", "ORGANIZATION", "ADMIN")
                        .requestMatchers("/api/lms/reviews/**").authenticated()
                        .requestMatchers("/api/lms/progress/**").authenticated()
                        .requestMatchers("/api/lms/export/**").authenticated()
                        .requestMatchers("/api/lms/announcements/**").authenticated()
                        .requestMatchers("/api/lms/certificates/**").authenticated()
                        .requestMatchers("/api/lms/notifications/**").authenticated()
                        
                        // Messaging endpoints (authenticated users)
                        .requestMatchers("/api/lms/messaging/**").authenticated()
                        
                        // WebSocket endpoint (authentication handled by interceptor)
                        .requestMatchers("/ws/**").permitAll()
                        
                        // Organization endpoints
                        .requestMatchers("/api/lms/organization/**").hasAnyRole("ORGANIZATION", "ADMIN")
                        
                        // Admin endpoints
                        .requestMatchers("/api/lms/admin/**").hasRole("ADMIN")
                        
                        // Payment endpoints (authenticated users)
                        .requestMatchers("/api/lms/payments/**").authenticated()
                        
                        // Coupon and Offer endpoints
                        .requestMatchers("/api/lms/coupons/active", "GET").permitAll() // Public: view active coupons
                        .requestMatchers("/api/lms/coupons/validate", "POST").authenticated() // Authenticated: validate coupon
                        .requestMatchers("/api/lms/coupons", "POST", "PUT", "DELETE").hasRole("ADMIN") // Admin: manage coupons
                        .requestMatchers("/api/lms/coupons/**", "PUT", "DELETE").hasRole("ADMIN") // Admin: manage coupons
                        .requestMatchers("/api/lms/offers/active", "GET").permitAll() // Public: view active offers
                        .requestMatchers("/api/lms/offers/course/**", "GET").permitAll() // Public: view offers for course
                        .requestMatchers("/api/lms/offers", "POST", "PUT", "DELETE").hasRole("ADMIN") // Admin: manage offers
                        .requestMatchers("/api/lms/offers/**", "PUT", "DELETE").hasRole("ADMIN") // Admin: manage offers
                        
                        // Recommendations and Wishlist
                        .requestMatchers("/api/lms/recommendations/**").authenticated()
                        .requestMatchers("/api/lms/wishlist/**").authenticated()
                        
                        // Refunds
                        .requestMatchers("/api/lms/refunds/my-refunds", "GET").authenticated() // Students: view their refunds
                        .requestMatchers("/api/lms/refunds/request/**", "POST").authenticated() // Students: request refund
                        .requestMatchers("/api/lms/refunds/pending", "GET").hasRole("ADMIN") // Admin: view pending refunds
                        .requestMatchers("/api/lms/refunds/all", "GET").hasRole("ADMIN") // Admin: view all refunds
                        .requestMatchers("/api/lms/refunds/*/approve", "POST").hasRole("ADMIN") // Admin: approve refund
                        .requestMatchers("/api/lms/refunds/*/reject", "POST").hasRole("ADMIN") // Admin: reject refund
                        
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("text/html;charset=UTF-8");
                            response.getWriter().write("""
                                <html>
                                <head><title>Access Denied</title></head>
                                <body>
                                    <h1>Access Denied</h1>
                                    <p>You don't have permission to access this resource.</p>
                                    <a href="/ui/lms">Go to Home</a>
                                </body>
                                </html>
                                """);
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
