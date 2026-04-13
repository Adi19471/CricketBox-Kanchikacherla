package com.sports.NetsCricket.security;


import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sports.NetsCricket.service.CustomUserDetailsService;
import com.sports.NetsCricket.utils.JWTUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
    	
    	 String path = request.getServletPath();

    	    // ✅ Skip public URLs
    	    if (path.startsWith("/auth") ||
    	        path.startsWith("/swagger-ui") ||
    	        path.startsWith("/v3/api-docs") ||
    	        path.equals("/payment.html") ||
    	        path.equals("/success.html") ||
    	        path.equals("/failure.html")) {

    	        filterChain.doFilter(request, response);
    	        return;
    	    }

        final String authHeader = request.getHeader("Authorization");

        // ✅ 1. Check header exists and starts with Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = authHeader.substring(7);

        // ✅ 2. Validate token format (must contain 2 dots)
        if (jwtToken == null || jwtToken.trim().isEmpty() || !jwtToken.contains(".")) {
            filterChain.doFilter(request, response);
            return;
        }

        String userEmail = null;

        try {
            // ✅ 3. Extract username safely
            userEmail = jwtUtils.extractUsername(jwtToken);
        } catch (Exception e) {
            System.out.println("Invalid JWT Token: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 4. Authenticate user
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

            if (jwtUtils.isValidToken(jwtToken, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
