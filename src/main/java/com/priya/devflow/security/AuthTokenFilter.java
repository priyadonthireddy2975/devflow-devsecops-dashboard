package com.priya.devflow.security;

import com.priya.devflow.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final String apiKey;

    public AuthTokenFilter(JwtService jwtService, AppProperties props) {
        this.jwtService = jwtService;
        this.apiKey = props.apiKey();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null) {
            authenticate(token);
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        String key = request.getHeader("X-API-Key");
        return key == null || key.isBlank() ? null : key.trim();
    }

    private void authenticate(String token) {
        if (apiKey != null && !apiKey.isBlank() && matchesApiKey(token)) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "ci-ingest", null, List.of(new SimpleGrantedAuthority("ROLE_INGEST"))));
            return;
        }
        jwtService.parse(token).ifPresent(claims -> {
            String role = claims.get("role", String.class);
            if (role != null) {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
            }
        });
    }

    private boolean matchesApiKey(String token) {
        return MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), apiKey.getBytes(StandardCharsets.UTF_8));
    }
}
