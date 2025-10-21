package ru.netology;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.entity.UserEntity;
import ru.netology.repository.UserRepo;
import ru.netology.service.AuthService;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private AuthService authService;

    @Test
    void getUserByToken_shouldReturnUser_whenTokenIsValidAndNotExpired() {
        // Given
        String token = "valid-token";
        UserEntity user = new UserEntity();
        user.setAuthToken(token);
        user.setTokenExpiry(Instant.now().plusSeconds(3600)); // +1 час

        when(userRepo.findByAuthToken(token)).thenReturn(user);

        // When
        UserEntity result = authService.getUserByToken(token);

        // Then
        assertNotNull(result);
        assertEquals(token, result.getAuthToken());
        verify(userRepo, never()).save(any()); // не должно сохранять
    }

    @Test
    void getUserByToken_shouldReturnNull_whenTokenIsNull() {
        // When
        UserEntity result = authService.getUserByToken(null);

        // Then
        assertNull(result);
        verify(userRepo, never()).findByAuthToken(any());
    }

    @Test
    void getUserByToken_shouldReturnNull_whenTokenIsEmpty() {
        // When
        UserEntity result = authService.getUserByToken("");

        // Then
        assertNull(result);
        verify(userRepo, never()).findByAuthToken(any());
    }

    @Test
    void getUserByToken_shouldReturnNull_whenUserNotFound() {
        // Given
        String token = "non-existent-token";
        when(userRepo.findByAuthToken(token)).thenReturn(null);

        // When
        UserEntity result = authService.getUserByToken(token);

        // Then
        assertNull(result);
    }

    @Test
    void getUserByToken_shouldReturnNull_andClearToken_whenTokenIsExpired() {
        // Given
        String token = "expired-token";
        UserEntity user = new UserEntity();
        user.setAuthToken(token);
        user.setTokenExpiry(Instant.now().minusSeconds(60)); // просрочен на 1 минуту

        when(userRepo.findByAuthToken(token)).thenReturn(user);

        // When
        UserEntity result = authService.getUserByToken(token);

        // Then
        assertNull(result);
        assertNull(user.getAuthToken());
        assertNull(user.getTokenExpiry());
        verify(userRepo).save(user); // должен сохранить очищенные поля
    }
}