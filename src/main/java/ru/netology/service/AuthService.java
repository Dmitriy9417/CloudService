package ru.netology.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.netology.entity.UserEntity;
import ru.netology.repository.UserRepo;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepo userRepo;

    public UserEntity getUserByToken(String token) {
        if (token == null || token.isBlank()) {
            log.debug("Received null or blank token");
            return null;
        }

        // Удаляем "Bearer ", если есть
        String cleanToken = token.trim();
        if (cleanToken.startsWith("Bearer ")) {
            cleanToken = cleanToken.substring(7).trim();
            log.trace("Removed 'Bearer ' prefix from token");
        }

        log.debug("Looking up user by clean token: [{}]", cleanToken);

        UserEntity user = userRepo.findByAuthToken(cleanToken);
        if (user == null) {
            log.warn("No user found for token: [{}]", cleanToken);
            return null;
        }

        // Проверяем, не истёк ли токен
        if (user.getTokenExpiry() != null && Instant.now().isAfter(user.getTokenExpiry())) {
            log.info("Token for user '{}' has expired. Cleaning token.", user.getLogin());

            user.setAuthToken(null);
            user.setTokenExpiry(null);
            userRepo.save(user);

            return null;
        }

        log.debug("Successfully authenticated user '{}' by token", user.getLogin());
        return user;
    }
}