package kg.taskflow.db.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "sys_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "phone")
    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    // Telegram integration fields
    @Column(name = "employee_code", unique = true, length = 8)
    private String employeeCode;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "telegram_username", length = 50)
    private String telegramUsername;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "sys_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive && !getIsDeleted();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Telegram helper methods
    public boolean isTelegramRegistered() {
        return telegramChatId != null;
    }

    public void generateEmployeeCode() {
        if (employeeCode == null) {
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder code = new StringBuilder("TF");
            java.util.Random random = new java.util.Random();
            for (int i = 0; i < 6; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
            this.employeeCode = code.toString();
        }
    }
}
