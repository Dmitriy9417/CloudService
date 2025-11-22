package ru.netology.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.netology.entity.UserEntity;
import ru.netology.repository.UserRepo;
import ru.netology.service.AuthService;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepo userRepo;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String login = body.get("login");
        String password = body.get("password");

        log.debug("Login attempt for user: {}", login);

        if (login == null || password == null) {
            log.warn("Login attempt with missing credentials");
            return ResponseEntity.badRequest().body(Map.of("message", "Missing email or password"));
        }

        UserEntity user = userRepo.findByLogin(login);
        if (user == null || !user.getPassword().equals(password)) {
            log.warn("Failed login attempt for user: {}", login);
            return ResponseEntity.badRequest().body(Map.of("message", "Bad credentials"));
        }

        String token = UUID.randomUUID().toString();
        user.setAuthToken(token);
        user.setTokenExpiry(Instant.now().plus(Duration.ofHours(1)));
        userRepo.save(user);
        log.info("User '{}' successfully logged in", login);
        return ResponseEntity.ok(Map.of("auth-token", token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("auth-token") String token) {
        log.debug("Logout request received");
        if (token == null) {
            log.warn("Logout attempt with null token");
            return ResponseEntity.badRequest().build();
        }

        UserEntity user = authService.getUserByToken(token);
        if (user != null) {
            user.setAuthToken(null);
            user.setTokenExpiry(null);
            userRepo.save(user);
            log.info("User '{}' successfully logged out", user.getLogin());
        } else {
            log.warn("Logout attempt with invalid token");
        }

        return ResponseEntity.ok().build();
    }
}