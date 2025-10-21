package ru.netology.controller;

import lombok.RequiredArgsConstructor;
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

    private final UserRepo userRepo;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String login = body.get("login");
        String password = body.get("password");

        if (login == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing email or password"));
        }

        UserEntity user = userRepo.findByLogin(login);
        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bad credentials"));
        }

        String token = UUID.randomUUID().toString();
        user.setAuthToken(token);
        user.setTokenExpiry(Instant.now().plus(Duration.ofHours(1)));
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("auth-token", token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("auth-token") String token) {
        if (token == null) return ResponseEntity.badRequest().build();

        UserEntity user = authService.getUserByToken(token); // ← используем сервис
        if (user != null) {
            user.setAuthToken(null);
            user.setTokenExpiry(null);
            userRepo.save(user);
        }

        return ResponseEntity.ok().build();
    }
}