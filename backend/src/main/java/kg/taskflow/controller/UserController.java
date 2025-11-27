package kg.taskflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.user.ChangePasswordRequest;
import kg.taskflow.dto.user.UpdateProfileRequest;
import kg.taskflow.dto.user.UserDto;
import kg.taskflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для работы с пользователями")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Получить текущего пользователя")
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/me")
    @Operation(summary = "Обновить профиль текущего пользователя")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Изменить пароль текущего пользователя")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Получить список пользователей")
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск пользователей")
    public ResponseEntity<List<UserDto>> searchUsers(
            @RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }
}
