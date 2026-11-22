package com.ecommerceproject.UserService.Service;

import com.ecommerceproject.UserService.Dto.LoginResponseDto;
import com.ecommerceproject.UserService.Dto.UserDto;
import com.ecommerceproject.UserService.Exception.InvalidCredentialException;
import com.ecommerceproject.UserService.Exception.InvalidSessionLogoutException;
import com.ecommerceproject.UserService.Exception.UserNotFoundException;
import com.ecommerceproject.UserService.Mapper.UserEntityDtoMapper;
import com.ecommerceproject.UserService.Model.Session;
import com.ecommerceproject.UserService.Model.SessionStatus;
import com.ecommerceproject.UserService.Model.User;
import com.ecommerceproject.UserService.Messaging.UserEventPublisher;
import com.ecommerceproject.UserService.Repository.SessionRepository;
import com.ecommerceproject.UserService.Repository.UserRepository;
import com.ecommerceproject.common.dto.ValidateTokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AuthService {

    private static final long SESSION_VALIDITY_DAYS = 3;

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserEventPublisher userEventPublisher;
    private final SecretKey jwtKey;

    public AuthService(UserRepository userRepository,
                       SessionRepository sessionRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       @Value("${jwt.secret}") String jwtSecret,
                       UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userEventPublisher = userEventPublisher;
        // One stable signing key for the whole application lifetime (loaded from config),
        // so tokens remain verifiable across requests, restarts and instances.
        this.jwtKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public ResponseEntity<LoginResponseDto> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found in the database"));

        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialException("password is incorrect for the given id");
        }

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(SESSION_VALIDITY_DAYS, ChronoUnit.DAYS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoleSet().stream().map(role -> role.getRoleName()).toList());

        String token = Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(user.getUserId()))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(jwtKey)
                .compact();

        Session session = new Session();
        session.setToken(token);
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setUser(user);
        session.setUserId(user.getUserId());
        session.setExpiringAt(Date.from(expiresAt));
        session.setLogInAt(Date.from(issuedAt));
        sessionRepository.save(session);

        LoginResponseDto loginResponse = new LoginResponseDto(token, user.getUserId(), user.getEmail());

        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token=" + token);

        return new ResponseEntity<>(loginResponse, headers, HttpStatus.OK);
    }

    public ResponseEntity<Void> logout(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUserId(token, userId);

        if (sessionOptional.isEmpty()) {
            throw new InvalidSessionLogoutException("Session you're trying to log out doesn't exist");
        }

        Session session = sessionOptional.get();
        session.setSessionStatus(SessionStatus.ENDED);
        sessionRepository.save(session);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public UserDto signUp(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.CONFLICT, "An account with this email already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        User savedUser = userRepository.save(user);

        userEventPublisher.publishUserSignup(savedUser.getUserId(), savedUser.getEmail());

        return UserDto.from(savedUser);
    }

    public SessionStatus validate(Long userId, String token) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUserId(token, userId);

        if (sessionOptional.isEmpty() || sessionOptional.get().getSessionStatus().equals(SessionStatus.ENDED)) {
            throw new UserNotFoundException("Invalid token for the given user id");
        }

        try {
            // Verifies the signature with the stable key and enforces the expiration claim.
            Jwts.parser()
                    .verifyWith(jwtKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException ex) {
            throw new InvalidCredentialException("Token signature/expiry validation failed");
        }

        return SessionStatus.ACTIVE;
    }

    /**
     * Resolves and verifies a bearer token for cross-service calls. The caller's
     * id is taken from the verified JWT subject (set at login), so the consumer
     * only needs to forward the token. Returns valid=false on any failure.
     */
    public ValidateTokenResponse validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.valueOf(claims.getSubject());

            Optional<Session> sessionOptional = sessionRepository.findByTokenAndUserId(token, userId);
            if (sessionOptional.isEmpty()
                    || sessionOptional.get().getSessionStatus().equals(SessionStatus.ENDED)) {
                return new ValidateTokenResponse(null, false, List.of());
            }

            List<String> roles = extractRoles(claims);
            return new ValidateTokenResponse(userId, true, roles);
        } catch (JwtException | NumberFormatException ex) {
            return new ValidateTokenResponse(null, false, List.of());
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    // Used for testing/inspection only; not intended for production exposure.
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }
}
