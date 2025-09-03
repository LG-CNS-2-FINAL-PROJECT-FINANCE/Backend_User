package com.ddiring.backend_user.controller;

import com.ddiring.backend_user.common.dto.ApiResponseDto;
import com.ddiring.backend_user.dto.UserDTO;
import com.ddiring.backend_user.dto.request.SignDto;
import com.ddiring.backend_user.dto.UserNameDto;
import com.ddiring.backend_user.service.ApiUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ApiUserController {

    private final ApiUserService apiUserService;

    @PostMapping("/detail")
    public List<UserDTO> getUserInfo(@RequestBody List<String> userInfo) {
        return apiUserService.getUserInfo(userInfo);
    }

    // 판매 주문 접수 서명 준비
    @PostMapping("/sell/sign")
    public ApiResponseDto<SignDto> signReady(@RequestBody SignDto signDto) {
        return ApiResponseDto.createOk(signDto);
    }

    @GetMapping("/{userSeq}")
    public Optional<UserNameDto> getUserName(@PathVariable String userSeq) {
        return apiUserService.getUserName(userSeq);
    }
}
