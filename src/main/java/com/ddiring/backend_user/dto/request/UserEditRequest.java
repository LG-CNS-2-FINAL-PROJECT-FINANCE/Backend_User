package com.ddiring.backend_user.dto.request;

import com.ddiring.backend_user.entity.User;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEditRequest {

    private Integer userSeq;
    private String nickname;
    private LocalDate updateAt;
}
