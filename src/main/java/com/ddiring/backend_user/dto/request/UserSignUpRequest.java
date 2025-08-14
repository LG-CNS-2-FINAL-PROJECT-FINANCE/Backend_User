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

    private Integer userSeq;
    private Integer created_id;
    private Integer updated_id;
}
