package com.ddiring.backend_user.dto.request;

import com.ddiring.backend_user.entity.User.Gender;
import com.ddiring.backend_user.entity.User.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAdditionalInfoRequest {
    
    private String userSeq;
    private String email;
    private String userName;
    private String nickname;
    private Role role;
    private Gender gender;
    private LocalDate birthDate;
}
