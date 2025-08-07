package com.ddiring.backend_user.dto.response;

import com.ddiring.backend_user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private String email;
    private String nickname;
    private User.Gender gender;
    private LocalDate birthDate;
}
