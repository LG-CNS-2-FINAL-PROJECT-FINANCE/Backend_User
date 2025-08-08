package com.ddiring.backend_user.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_seq", nullable = false, unique = true)
    private Integer userSeq;

    @Column(name = "kakao_id", unique = true)
    private String kakaoId;

    @Column(name = "admin_id", unique = true)
    private String adminId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status")
    private UserStatus user_status;

    @Column(name = "latest_at", nullable = false)
    private LocalDateTime latestAt;

    @Column(name = "created_id", nullable = false)
    private Integer createdId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_id", nullable = false)
    private Integer updatedId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Role {
        ADMIN, USER, CREATOR
    }

    public enum Gender {
        MALE, FEMALE
    }

    public enum UserStatus {
        ACTIVE, DISABLED, DELETED
    }

    public void updateUserStatus(UserStatus status) {
        this.user_status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateUpdatedInfo(Integer updatedId) {
        this.updatedId = updatedId;
        this.updatedAt = LocalDateTime.now();
    }
}
