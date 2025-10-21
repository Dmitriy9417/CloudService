package ru.netology;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.entity.UserEntity;
import ru.netology.repository.UserRepo;
import ru.netology.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginSuccess() {
        UserEntity user = new UserEntity();
        user.setLogin("test@example.com");
        user.setPassword("password");

        when(userRepo.findByLogin("test@example.com")).thenReturn(user);

        String token = authService.login("test@example.com", "password");

        assertNotNull(token);
        verify(userRepo).save(user);
    }

    @Test
    void loginBadCredentials() {
        when(userRepo.findByLogin("bad@example.com")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> authService.login("bad@example.com", "123"));
    }
}
