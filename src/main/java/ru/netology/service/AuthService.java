package ru.netology.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.netology.entity.UserEntity;
import ru.netology.repository.UserRepo;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;

    public UserEntity getUserByToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        // Удаляем "Bearer ", если есть
        String cleanToken = token.trim();
        if (cleanToken.startsWith("Bearer ")) {
            cleanToken = cleanToken.substring(7).trim();
        }
        System.out.println("Clean token: [" + cleanToken + "]");

        UserEntity user = userRepo.findByAuthToken(cleanToken);
        if (user == null) {
            return null;
        }

        // Проверяем, не истёк ли токен
        if (user.getTokenExpiry() != null && Instant.now().isAfter(user.getTokenExpiry())) {
            // Токен просрочен — очищаем его
            user.setAuthToken(null);
            user.setTokenExpiry(null);
            userRepo.save(user);
            return null;
        }

        return user;
    }
}