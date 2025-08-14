package com.ddiring.backend_user.dto.request;

import com.ddiring.backend_user.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter
@Setter
public class UserLoginRequest {

    private Integer userSeq;
    private String userName;
    private String nickname;
    private User.Role role;
    private User.Gender gender;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;
    private Integer age;
}
