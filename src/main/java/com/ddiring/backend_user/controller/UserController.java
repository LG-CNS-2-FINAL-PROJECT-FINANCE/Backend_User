package com.ddiring.backend_user.controller;

import com.ddiring.backend_user.dto.request.AdminRequest;
import com.ddiring.backend_user.dto.request.UserEditRequest;
import com.ddiring.backend_user.dto.request.UserLoginRequest;
import com.ddiring.backend_user.dto.request.UserSignUpRequest;
import com.ddiring.backend_user.dto.request.UserAdditionalInfoRequest;
import com.ddiring.backend_user.dto.response.UserInfoResponse;
import com.ddiring.backend_user.dto.response.UserListResponse;
import com.ddiring.backend_user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 가입(카카오 이메일 등록)
    @PostMapping("/auth/oauth/signup")
    public ResponseEntity<String> kakaoSignup(@RequestBody UserSignUpRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok("회원님의 추가 정보가 필요합니다.");
    }

    // 회원 정보 등록
    @PostMapping("/auth/signup")
    public ResponseEntity<String> additionalSignup(@RequestBody UserAdditionalInfoRequest request) {
        userService.signUpUser(request);
        return ResponseEntity.ok("회원가입이 완료되었습니다. 로그인 해주세요.");
    }

    // 회원 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<?> kakaoLoginPost(
            @RequestParam("code") String code,
            @RequestBody(required = false) UserLoginRequest request) {
        return userService.kakaoLoginWithRedirect(code, request);
    }

    // 관리자 회원가입
    @PostMapping("/auth/admin/signup")
    public ResponseEntity<?> registerAdmin(@RequestBody AdminRequest request) {
        return userService.registerAdmin(request);
    }

    // 관리자 로그인
    @PostMapping("/auth/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminRequest request) {
        return userService.adminLogin(request);
    }

    // 개인 정보 조회
    @GetMapping("/{userSeq}")
    @PreAuthorize("hasRole('ADMIN') or #userSeq == authentication.principal")
    public UserInfoResponse getUserInfo(@PathVariable String userSeq) {
        return userService.getUserInfo(userSeq);
    }

    // 모든 사용자 정보 조회
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserListResponse> getUserList() {
        return userService.getUserList();
    }

    // 회원 정보 수정
    @PostMapping("/auth/edit")
    public void editUser(@RequestBody UserEditRequest request) {
        userService.editUser(request);
    }

    // 로그아웃
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshTokenHeader) {
        return userService.logout(authHeader, refreshTokenHeader);
    }

    // 회원탈퇴
    @PostMapping("/auth/delete")
    public ResponseEntity<String> deleteUser(@RequestParam String userSeq) {
        return userService.deleteUserWithResponse(userSeq);
    }

    // 역할 토글
    @PostMapping("/auth/role-toggle")
    public ResponseEntity<String> toggleUserRole(@RequestParam String userSeq) {
        return userService.toggleUserRoleWithResponse(userSeq);
    }
}
