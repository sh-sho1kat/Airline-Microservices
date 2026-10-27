package com.sho1kat.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null
                && authHeader.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(authHeader.substring(7), request);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        try {
            if (!jwtProvider.isTokenValid(token)) {
                return;
            }

            String subject = jwtProvider.extractSubject(token);
            if (subject == null || subject.isBlank()) {
                return;
            }

            Set<SimpleGrantedAuthority> authorities = jwtProvider.extractRoles(token).stream()
                    .filter(role -> role != null && !role.isBlank())
                    .collect(LinkedHashSet::new,
                            (set, role) -> set.addAll(expandRoles(role)),
                            Set::addAll);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            subject,
                            null,
                            List.copyOf(authorities)
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // Tampered or unparsable token: stay unauthenticated.
            SecurityContextHolder.clearContext();
        }
    }

    /*
     * Roles may arrive with or without the ROLE_ prefix. user-service names its
     * administrator role ADMIN, while pricing documents its admin authority as
     * AIRLINE_ADMIN, so both names are aliases here. Trusted service roles such as
     * FLIGHT_OPS_SERVICE and BOOKING_SERVICE pass through unchanged when present.
     */
    private Set<SimpleGrantedAuthority> expandRoles(String role) {

        String normalized = "ROLE_"
                + (role.startsWith("ROLE_")
                        ? role.substring(5)
                        : role)
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .trim();

        if ("ROLE_".equals(normalized)) {
            return Set.of();
        }

        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new SimpleGrantedAuthority(normalized));

        if ("ROLE_ADMIN".equals(normalized)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_AIRLINE_ADMIN"));
        } else if ("ROLE_AIRLINE_ADMIN".equals(normalized)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return authorities;
    }
}
