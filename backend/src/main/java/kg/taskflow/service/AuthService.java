package kg.taskflow.service;

import kg.taskflow.dto.auth.AuthResponse;
import kg.taskflow.dto.auth.LoginRequest;
import kg.taskflow.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    boolean verifyResetToken(String token);
}
