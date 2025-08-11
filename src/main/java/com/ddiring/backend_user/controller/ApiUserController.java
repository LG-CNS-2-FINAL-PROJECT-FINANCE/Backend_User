package com.ddiring.backend_user.controller;

import com.ddiring.backend_user.dto.UserDTO;
import com.ddiring.backend_user.service.ApiUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ApiUserController {

    private final ApiUserService apiUserService;

    @PostMapping("/detail")
    public List<UserDTO> getUserInfo(@RequestBody List<Integer> userInfo) {
        return apiUserService.getUserInfo(userInfo);
    }
}

