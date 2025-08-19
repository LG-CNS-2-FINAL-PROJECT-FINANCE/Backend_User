package com.ddiring.backend_user.dto.request;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEditRequest {

    private String userSeq;
    private String nickname;
    private LocalDateTime updateAt;

    public String getNickname() { return nickname; }
}
