package ru.netology.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.netology.entity.UserEntity;
import ru.netology.repository.UserRepo;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;


    public String login(String email, String password) {
        UserEntity user = userRepo.findByLogin(email);

        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("Bad credentials");
        }

        String token = UUID.randomUUID().toString();
        user.setAuthToken(token);
        userRepo.save(user);

        return token;
    }


    public void logout(String token) {
        if (token == null) return;

        UserEntity user = userRepo.findByAuthToken(token);
        if (user != null) {
            user.setAuthToken(null);
            userRepo.save(user);
        }
    }


    public UserEntity getUserByToken(String token) {
        if (token == null) return null;
        return userRepo.findByAuthToken(token);
    }
}
