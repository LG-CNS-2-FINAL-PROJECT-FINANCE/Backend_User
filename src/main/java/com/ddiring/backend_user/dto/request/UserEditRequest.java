package com.ddiring.backend_user.dto.request;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEditRequest {

    private String nickname;
    private LocalDateTime updateAt;
}
