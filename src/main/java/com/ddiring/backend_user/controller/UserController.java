package com.ddiring.backend_user.controller;

import com.ddiring.backend_user.dto.request.*;
import com.ddiring.backend_user.dto.response.UserInfoResponse;
import com.ddiring.backend_user.dto.response.UserListResponse;
import com.ddiring.backend_user.entity.User.Role;
import com.ddiring.backend_user.entity.User.UserStatus;
import com.ddiring.backend_user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<?> kakaoLoginPost(
            @RequestParam("code") String code,
            @RequestBody(required = false) UserLoginRequest request) {
        return userService.kakaoLogin(code, request);
    }

    // 회원 정보 등록
    @PostMapping("/register")
    public ResponseEntity<String> additionalSignup(Authentication authentication,
            @RequestBody UserAdditionalInfoRequest request) {
        String userSeq = (String) authentication.getPrincipal();
        userService.signUpUser(userSeq, request);
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
    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'GUEST', 'ADMIN')")
    public UserInfoResponse getMyInfo(Authentication authentication) {
        String userSeq = (String) authentication.getPrincipal();
        return userService.getUserInfo(userSeq);
    }

    // 모든 사용자 정보 조회 (관리자 전용)
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserListResponse> getUserList() {
        return userService.getUserList();
    }

    // 사용자 검색 (관리자 전용)
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserListResponse> searchUsers(
            @RequestParam String searchType,
            @RequestParam String search) {
        return userService.searchUsers(searchType, search);
    }

    // 회원 정보 수정
    @PostMapping("/edit")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'GUEST', 'ADMIN')")
    public void editUser(Authentication authentication, @RequestBody UserEditRequest request) {
        String userSeq = (String) authentication.getPrincipal();
        userService.editUser(userSeq, request);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshTokenHeader) {
        return userService.logout(authHeader, refreshTokenHeader);
    }

    // 회원탈퇴
    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'GUEST', 'ADMIN')")
    public ResponseEntity<String> deleteUser(Authentication authentication) {
        String userSeq = (String) authentication.getPrincipal();
        return userService.deleteUserWithResponse(userSeq);
    }

    // 역할 선택
    @PostMapping("/role")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> selectRole(
            Authentication authentication,
            @RequestParam(name = "role", required = false) Role role) {
        String userSeq = (String) authentication.getPrincipal();
        return userService.selectRole(userSeq, role);
    }

    // 사용자 상태 변경(활성화)
    @PostMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changeStatusActive(
            @RequestParam String userSeq,
            @RequestParam(name = "user_status", required = false) UserStatus status) {
        UserStatus desired = UserStatus.ACTIVE;
        if (status != null && status != desired) {
            return ResponseEntity.badRequest().body("요청한 엔드포인트와 user_status 값이 일치하지 않습니다. (ACTIVE)");
        }
        return userService.updateUserStatusWithResponse(userSeq, desired);
    }

    // 사용자 상태 변경(비활성화)
    @PostMapping("/disabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changeStatusDisabled(
            @RequestParam String userSeq,
            @RequestParam(name = "user_status", required = false) UserStatus status) {
        UserStatus desired = UserStatus.DISABLED;
        if (status != null && status != desired) {
            return ResponseEntity.badRequest().body("요청한 엔드포인트와 user_status 값이 일치하지 않습니다. (DISABLED)");
        }
        return userService.updateUserStatusWithResponse(userSeq, desired);
    }

    // 사용자 상태 변경(정지/삭제)
    @PostMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changeStatusDeleted(
            @RequestParam String userSeq,
            @RequestParam(name = "user_status", required = false) UserStatus status) {
        UserStatus desired = UserStatus.DELETED;
        if (status != null && status != desired) {
            return ResponseEntity.badRequest().body("요청한 엔드포인트와 user_status 값이 일치하지 않습니다. (DELETED)");
        }
        return userService.updateUserStatusWithResponse(userSeq, desired);
    }

}
