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
    @Column(name = "user_seq", unique = true)
    private Integer userSeq;

    @Column(name = "kakao_id")
    private String kakaoId;

    @Column(name = "admin_id")
    private String adminId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status")
    private UserStatus user_status;

    @Column(name = "bank_type")
    private Integer bankType;

    @Column(name = "latest_at")
    private LocalDateTime latestAt;

    @Column(name = "created_id")
    private Integer createdId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_id")
    private Integer updatedId;

    @Column(name = "updated_at")
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
