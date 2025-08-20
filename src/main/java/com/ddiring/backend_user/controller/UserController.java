package com.ddiring.backend_user.controller;

import com.ddiring.backend_user.dto.request.AdminRequest;
import com.ddiring.backend_user.dto.request.UserEditRequest;
import com.ddiring.backend_user.dto.request.UserLoginRequest;
import com.ddiring.backend_user.dto.request.UserAdditionalInfoRequest;
import com.ddiring.backend_user.dto.response.UserInfoResponse;
import com.ddiring.backend_user.dto.response.UserListResponse;
import com.ddiring.backend_user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.parameters.P;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<?> kakaoLoginGet(@RequestParam("code") String code) {
        return userService.kakaoLoginWithRedirect(code, null);
    }

    // 회원 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<?> kakaoLoginPost(
            @RequestParam("code") String code,
            @RequestBody(required = false) UserLoginRequest request) {
        return userService.kakaoLogin(code, request);
    }

    // 회원 정보 등록
    @PostMapping("/auth/register")
    public ResponseEntity<String> additionalSignup(@RequestBody UserAdditionalInfoRequest request) {
        userService.signUpUser(request);
        return ResponseEntity.ok("회원님의 추가 정보가 등록되었습니다.");
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
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public UserInfoResponse getMyInfo(Authentication authentication) {
        String userSeq = (String) authentication.getPrincipal();
        return userService.getUserInfo(userSeq);
    }

    // 모든 사용자 정보 조회 (관리자 전용)
    @GetMapping("/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserListResponse> getUserList() {
        return userService.getUserList();
    }

    // 회원 정보 수정
    @PostMapping("/auth/edit")
    @PreAuthorize("hasRole('ADMIN') or #request.userSeq == authentication.principal")
    public void editUser(@P("request") @RequestBody UserEditRequest request) {
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
    @PreAuthorize("hasRole('ADMIN') or #userSeq == authentication.principal")
    public ResponseEntity<String> deleteUser(@P("userSeq") @RequestParam String userSeq) {
        return userService.deleteUserWithResponse(userSeq);
    }

    // 역할 선택
    @PostMapping("/auth/role")
    @PreAuthorize("hasRole('ADMIN') or #userSeq == authentication.principal")
    public ResponseEntity<String> selectRole(
            @RequestParam String userSeq,
            @RequestParam(name = "role") com.ddiring.backend_user.entity.User.Role role) {
        return userService.selectRole(userSeq, role);
    }

    // 역할 토글
    @PostMapping("/auth/role-toggle")
    @PreAuthorize("hasRole('ADMIN') or #userSeq == authentication.principal")
    public ResponseEntity<String> toggleUserRole(@RequestParam String userSeq) {
        return userService.toggleUserRoleWithResponse(userSeq);
    }

    // 사용자 상태 변경(활성화)
    @PostMapping("/auth/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changeStatusActive(
            @RequestParam String userSeq,
            @RequestParam(name = "user_status", required = false) com.ddiring.backend_user.entity.User.UserStatus status) {
        com.ddiring.backend_user.entity.User.UserStatus desired = com.ddiring.backend_user.entity.User.UserStatus.ACTIVE;
        if (status != null && status != desired) {
            return ResponseEntity.badRequest().body("요청한 엔드포인트와 user_status 값이 일치하지 않습니다. (ACTIVE)");
        }
        return userService.updateUserStatusWithResponse(userSeq, desired);
    }

    // 사용자 상태 변경(비활성화)
    @PostMapping("/auth/disabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changeStatusDisabled(
            @RequestParam String userSeq,
            @RequestParam(name = "user_status", required = false) com.ddiring.backend_user.entity.User.UserStatus status) {
        com.ddiring.backend_user.entity.User.UserStatus desired = com.ddiring.backend_user.entity.User.UserStatus.DISABLED;
        if (status != null && status != desired) {
            return ResponseEntity.badRequest().body("요청한 엔드포인트와 user_status 값이 일치하지 않습니다. (DISABLED)");
        }
        return userService.updateUserStatusWithResponse(userSeq, desired);
    }

    // 사용자 상태 변경(정지/삭제)
    @PostMapping("/auth/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changeStatusDeleted(
            @RequestParam String userSeq,
            @RequestParam(name = "user_status", required = false) com.ddiring.backend_user.entity.User.UserStatus status) {
        com.ddiring.backend_user.entity.User.UserStatus desired = com.ddiring.backend_user.entity.User.UserStatus.DELETED;
        if (status != null && status != desired) {
            return ResponseEntity.badRequest().body("요청한 엔드포인트와 user_status 값이 일치하지 않습니다. (DELETED)");
        }
        return userService.updateUserStatusWithResponse(userSeq, desired);
    }

}
