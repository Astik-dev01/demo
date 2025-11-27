package kg.taskflow.service.impl;

import kg.taskflow.db.entity.User;
import kg.taskflow.db.repository.UserRepository;
import kg.taskflow.dto.user.ChangePasswordRequest;
import kg.taskflow.dto.user.UpdateProfileRequest;
import kg.taskflow.dto.user.UserDto;
import kg.taskflow.exception.BadRequestException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.UserMapper;
import kg.taskflow.service.FileService;
import kg.taskflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    @Override
    public User getById(UUID id) {
        return userRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Override
    public UserDto getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateCurrentUserProfile(UpdateProfileRequest request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user = getById(user.getId()); // Get managed entity
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user = getById(user.getId()); // Get managed entity

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDto updateProfile(UUID id, String firstName, String lastName, String phone) {
        User user = getById(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void updateAvatar(UUID id, String avatarUrl) {
        User user = getById(id);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user = getById(user.getId());

        // Delete old avatar if exists
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                // Extract file path from URL
                String oldPath = user.getAvatarUrl();
                if (oldPath.contains("/avatars/")) {
                    int idx = oldPath.indexOf("/avatars/");
                    oldPath = oldPath.substring(idx + 1);
                    fileService.deleteFile(oldPath);
                }
            } catch (Exception e) {
                // Ignore deletion errors
            }
        }

        // Upload new avatar
        String path = "avatars/" + user.getId();
        String filePath = fileService.uploadFile(file, path);
        String avatarUrl = fileService.getPublicUrl(filePath);

        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }

    @Override
    @Transactional
    public void deleteAvatar() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user = getById(user.getId());

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                String oldPath = user.getAvatarUrl();
                if (oldPath.contains("/avatars/")) {
                    int idx = oldPath.indexOf("/avatars/");
                    oldPath = oldPath.substring(idx + 1);
                    fileService.deleteFile(oldPath);
                }
            } catch (Exception e) {
                // Ignore deletion errors
            }
        }

        user.setAvatarUrl(null);
        userRepository.save(user);
    }

    @Override
    public List<UserDto> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(query.trim()).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAllActive().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}
