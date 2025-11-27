package kg.taskflow.service.impl;

import kg.taskflow.db.entity.PasswordResetToken;
import kg.taskflow.db.entity.RefreshToken;
import kg.taskflow.db.entity.Role;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.repository.PasswordResetTokenRepository;
import kg.taskflow.db.repository.RefreshTokenRepository;
import kg.taskflow.db.repository.RoleRepository;
import kg.taskflow.db.repository.UserRepository;
import kg.taskflow.dto.auth.AuthResponse;
import kg.taskflow.dto.auth.LoginRequest;
import kg.taskflow.dto.auth.RegisterRequest;
import kg.taskflow.exception.BadRequestException;
import kg.taskflow.exception.ConflictException;
import kg.taskflow.exception.UnauthorizedException;
import kg.taskflow.mapper.UserMapper;
import kg.taskflow.security.jwt.JwtService;
import kg.taskflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new BadRequestException("Default role not found"));

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .roles(Set.of(userRole))
                .build();

        user = userRepository.save(user);
        return generateAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findActiveByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        return generateAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!storedToken.isValid()) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = storedToken.getUser();
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return generateAuthResponse(user);
    }

    @Override
    public void logout(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElse(null);

        if (storedToken != null) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
        }
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findActiveByEmail(email).orElse(null);

        // Always return success to prevent email enumeration
        if (user == null) {
            log.info("Password reset requested for non-existent email: {}", email);
            return;
        }

        // Invalidate any existing tokens
        passwordResetTokenRepository.invalidateUserTokens(user.getId());

        // Generate new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        passwordResetTokenRepository.save(resetToken);

        // TODO: Send email with reset link
        log.info("Password reset token generated for user: {}", email);
        log.debug("Reset token: {}", token); // Remove in production
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        // Invalidate all refresh tokens for security
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyResetToken(String token) {
        return passwordResetTokenRepository
                .findValidToken(token, LocalDateTime.now())
                .isPresent();
    }
}
