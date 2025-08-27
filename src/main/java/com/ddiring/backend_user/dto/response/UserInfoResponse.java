package com.ddiring.backend_user.dto.response;

import com.ddiring.backend_user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private String email;
    private String nickname;
    private User.Role role;
    private LocalDate birthDate;
    private Integer age;
    private User.Gender gender;
    private User.UserStatus status;
    private LocalDateTime latestAt;
}
