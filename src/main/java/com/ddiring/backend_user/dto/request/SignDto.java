package com.ddiring.backend_user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignDto {
    private String projectId;
    private String userAddress;
    private Integer tokenQuantity;
}
