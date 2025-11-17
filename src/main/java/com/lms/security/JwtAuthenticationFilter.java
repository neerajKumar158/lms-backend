package com.lms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        
        // Skip JWT processing for UI routes, static resources, and auth endpoints
        if (requestURI.startsWith("/ui/") || 
            requestURI.startsWith("/api/auth/") ||
            requestURI.startsWith("/static/") ||
            requestURI.startsWith("/uploads/") ||
            requestURI.startsWith("/h2-console/") ||
            requestURI.equals("/") ||
            requestURI.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null) {
            String lower = authHeader.toLowerCase();
            int idx = lower.indexOf("bearer ");
            if (idx == 0) {
                token = authHeader.substring(7).trim();
            }
        } else {
            String qpToken = request.getParameter("token");
            if (qpToken != null && !qpToken.isBlank()) {
                token = qpToken;
            }
        }
        if (token != null) {
            try {
                String username = jwtService.extractSubject(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = userDetailsService.loadUserByUsername(username);
                    var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // Log JWT validation errors for debugging
                System.err.println("JWT validation error for request " + requestURI + ": " + e.getMessage());
                // Don't set authentication if token is invalid
            }
        }
        filterChain.doFilter(request, response);
    }
}



