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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить аватар пользователя")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(file);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }

    @DeleteMapping("/me/avatar")
    @Operation(summary = "Удалить аватар пользователя")
    public ResponseEntity<Void> deleteAvatar() {
        userService.deleteAvatar();
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
