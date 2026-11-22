package com.ecommerceproject.UserService.Service;

import com.ecommerceproject.UserService.Dto.LoginResponseDto;
import com.ecommerceproject.UserService.Exception.UserNotFoundException;
import com.ecommerceproject.UserService.Messaging.UserEventPublisher;
import com.ecommerceproject.UserService.Model.Session;
import com.ecommerceproject.UserService.Model.SessionStatus;
import com.ecommerceproject.UserService.Model.User;
import com.ecommerceproject.UserService.Repository.SessionRepository;
import com.ecommerceproject.UserService.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    // A valid Base64-encoded secret (>= 256 bits once decoded).
    private static final String SECRET =
            "isk7Bx5VWrClcAuui12GBJ3GEM17F/awnPWHLhYC7uNvjH44ZKl7hldApRWRzTxn";
    private static final long THREE_DAYS_MILLIS = 3L * 24 * 60 * 60 * 1000;

    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder encoder;
    private AuthService authService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        sessionRepository = mock(SessionRepository.class);
        encoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, sessionRepository, encoder, SECRET,
                mock(UserEventPublisher.class));
    }

    @Test
    void loginIssuesVerifiableTokenAndThreeDaySession() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("a@b.com");
        user.setPassword(encoder.encode("pass"));
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        ResponseEntity<LoginResponseDto> response = authService.login("a@b.com", "pass");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        Session saved = captor.getValue();

        assertEquals(SessionStatus.ACTIVE, saved.getSessionStatus());

        long diff = saved.getExpiringAt().getTime() - System.currentTimeMillis();
        assertTrue(Math.abs(diff - THREE_DAYS_MILLIS) < 60_000,
                "session should expire ~3 days from now");

        // A token minted now must verify against the same (stable) key.
        String token = saved.getToken();
        when(sessionRepository.findByTokenAndUserId(token, 1L)).thenReturn(Optional.of(saved));
        assertEquals(SessionStatus.ACTIVE, authService.validate(1L, token));
    }

    @Test
    void validateRejectsEndedSession() {
        Session ended = new Session();
        ended.setSessionStatus(SessionStatus.ENDED);
        when(sessionRepository.findByTokenAndUserId("t", 1L)).thenReturn(Optional.of(ended));
        assertThrows(UserNotFoundException.class, () -> authService.validate(1L, "t"));
    }
}
