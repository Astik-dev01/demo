package kg.taskflow.service;

import kg.taskflow.db.entity.User;
import kg.taskflow.dto.user.ChangePasswordRequest;
import kg.taskflow.dto.user.UpdateProfileRequest;
import kg.taskflow.dto.user.UserDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User getById(UUID id);

    User getByEmail(String email);

    UserDto getCurrentUser();

    UserDto updateCurrentUserProfile(UpdateProfileRequest request);

    UserDto updateProfile(UUID id, String firstName, String lastName, String phone);

    void changePassword(ChangePasswordRequest request);

    void updateAvatar(UUID id, String avatarUrl);

    String uploadAvatar(MultipartFile file);

    void deleteAvatar();

    List<UserDto> searchUsers(String query);

    List<UserDto> getAllUsers();
}
