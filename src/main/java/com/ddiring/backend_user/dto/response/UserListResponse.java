package com.ddiring.backend_user.dto.response;

import com.ddiring.backend_user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserListResponse {

    private String userSeq;
    private String email;
    private String nickname;
    private User.Role role;
    private Integer age;
    private User.Gender gender;
    private User.UserStatus status;
    private LocalDateTime latestAt;
}
