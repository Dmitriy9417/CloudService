package ru.netology.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.entity.UserEntity;
import ru.netology.repository.UserRepo;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserRepo userRepo;

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
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("auth-token", token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("auth-token") String token) {
        if (token == null) return ResponseEntity.badRequest().build();

        UserEntity user = userRepo.findByAuthToken(token);
        if (user != null) {
            user.setAuthToken(null);
            userRepo.save(user);
        }

        return ResponseEntity.ok().build();
    }
}