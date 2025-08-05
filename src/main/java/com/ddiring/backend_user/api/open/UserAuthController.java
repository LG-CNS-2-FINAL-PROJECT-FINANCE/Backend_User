package com.ddiring.backend_user.api.open;

import com.ddiring.backend_user.common.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/user/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserAuthController {

//    @PostMapping(value = "/register")
//    public ApiResponseDto<String> register() {
//
//    }
//
//    @PostMapping(value = "/login")
//    public ApiResponseDto login() {
//
//    }
//
    @GetMapping(value = "/test")
    public ApiResponseDto<String> test() {
        return ApiResponseDto.createOk("카나리 버전입니다.");
    }
}