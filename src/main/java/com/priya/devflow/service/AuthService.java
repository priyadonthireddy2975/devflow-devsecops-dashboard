package com.priya.devflow.service;

import com.priya.devflow.dto.Dtos.LoginRequest;
import com.priya.devflow.dto.Dtos.LoginResponse;
import com.priya.devflow.entity.AppUser;
import com.priya.devflow.exception.InvalidCredentialsException;
import com.priya.devflow.exception.TooManyAttemptsException;
import com.priya.devflow.repository.UserRepository;
import com.priya.devflow.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public LoginResponse login(LoginRequest request) {
        String key = request.username().toLowerCase();
        ensureNotLocked(key);
        AppUser user = users.findByUsername(request.username()).orElse(null);
        if (user == null || !encoder.matches(request.password(), user.getPasswordHash())) {
            recordFailure(key);
            throw new InvalidCredentialsException();
        }
        failures.remove(key);
        Instant expiresAt = jwt.expiryFromNow();
        return new LoginResponse(jwt.generate(user, expiresAt), user.getUsername(), user.getRole(), expiresAt);
    }

    private void ensureNotLocked(String key) {
        Deque<Instant> attempts = failures.get(key);
        if (attempts == null) {
            return;
        }
        synchronized (attempts) {
            Instant cutoff = Instant.now().minus(WINDOW);
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
                attempts.pollFirst();
            }
            if (attempts.size() >= MAX_FAILURES) {
                throw new TooManyAttemptsException("Too many failed attempts. Try again in a few minutes.");
            }
        }
    }

    private void recordFailure(String key) {
        Deque<Instant> attempts = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (attempts) {
            attempts.addLast(Instant.now());
        }
    }
}
