package com.ddiring.backend_user.dto.request;

import com.ddiring.backend_user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpRequest {

    private String userName;
    private String nickname;
    private String email;
    private String kakaoId;
    private LocalDate birthDate;
    private User.Gender gender;
    private User.Role role;
}
