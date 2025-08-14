package com.ddiring.backend_user.controller;

import com.ddiring.backend_user.dto.request.AdminRequest;
import com.ddiring.backend_user.dto.request.UserEditRequest;
import com.ddiring.backend_user.dto.request.UserLoginRequest;
import com.ddiring.backend_user.dto.request.UserSignUpRequest;
import com.ddiring.backend_user.dto.response.UserInfoResponse;
import com.ddiring.backend_user.dto.response.UserListResponse;
import com.ddiring.backend_user.entity.User;
import com.ddiring.backend_user.kakao.KakaoOAuthService;
import com.ddiring.backend_user.redis.RedisService;
import com.ddiring.backend_user.repository.UserRepository;
import com.ddiring.backend_user.secret.jwt.JwtTokenProvider;
import com.ddiring.backend_user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final KakaoOAuthService kakaoOAuthService;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @PostMapping(value = "/auth/signup")
    public ResponseEntity<String> registerUser(@RequestBody @Valid UserSignUpRequest request) {
        userService.registerUser(request);

        return ResponseEntity.ok("회원 등록이 완료되었습니다.");
    }

    // 회원 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<?> kakaoLogin(
            @RequestParam("code") String code,
            @RequestBody UserLoginRequest request
            ) {
        String kakaoAccessToken = kakaoOAuthService.getAccessToken(code);
        KakaoOAuthService.KakaoUserInfo userInfo = kakaoOAuthService.getUserInfo(kakaoAccessToken);

        User.Role role = request.getRole();
        User.Gender gender = request.getGender();

        Optional<User> optionalUser = userRepository.findByKakaoId(userInfo.getKakaoId());

        User user = optionalUser.orElseGet(() -> {
            return userRepository.save(User.builder()
                    .kakaoId(userInfo.getKakaoId())
                    .email(userInfo.getEmail())
                    .userName(request.getUserName())
                    .nickname(request.getNickname())
                    .role(role)
                    .gender(gender)
                    .birthDate(request.getBirthDate())
                    .age(userService.updateAge(request.getBirthDate()))
                    .latestAt(LocalDateTime.now())
                    .user_status(User.UserStatus.ACTIVE)
                    .build());
        });

        String accessToken = jwtTokenProvider.createToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)
                .body(java.util.Map.of(
                    "message", "로그인 성공",
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
                ));
    }

    // 관리자 회원가입
    @PostMapping("/auth/admin/signup")
    public ResponseEntity<?> registerAdmin(@RequestBody AdminRequest request) {
        if (userRepository.existsByAdminId(request.getAdminId())) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }

        User admin = User.builder()
                .adminId(request.getAdminId())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ADMIN)
                .userName("관리자")
                .nickname("관리자")
                .latestAt(LocalDateTime.now())
                .createdId(0)
                .createdAt(LocalDateTime.now())
                .updatedId(0)
                .updatedAt(LocalDateTime.now())
                .user_status(User.UserStatus.ACTIVE)
                .build();

        userRepository.save(admin);

        return ResponseEntity.ok("관리자 등록 완료");
    }

    // 관리자 로그인
    @PostMapping("/auth/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminRequest request) {
        Optional<User> admin = userRepository.findByAdminId(request.getAdminId());

        if (admin.isEmpty()) {
            return ResponseEntity.badRequest().body("존재하지 않는 계정입니다.");
        }

        User user = admin.get();

        if (user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.badRequest().body("관리자 권한이 없습니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("아이디 또는 비밀번호가 틀렸습니다.");
        }

        String accessToken = jwtTokenProvider.adminCreateToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)
                .body(java.util.Map.of(
                    "message", "관리자 로그인 성공",
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
                ));
    }

    // 개인 정보 조회
    @GetMapping("/{userSeq}")
    public UserInfoResponse getUserInfo(@PathVariable Integer userSeq) {
        User user = userRepository.findByUserSeq(userSeq)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));

        return new UserInfoResponse(
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getAge(),
                user.getGender(),
                user.getUser_status(),
                user.getLatestAt()
        );
    }

    // 모든 사용자 정보 조회
    @GetMapping
    public List<UserListResponse> getUserList() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> new UserListResponse(
                        user.getUserSeq(),
                        user.getEmail(),
                        user.getNickname(),
                        user.getRole(),
                        user.getAge(),
                        user.getGender(),
                        user.getUser_status(),
                        user.getLatestAt()
                ))
                .collect(Collectors.toList());
    }

    // 회원 정보 수정
    @PostMapping("/auth/edit")
    public void editUser(@RequestBody UserEditRequest request) {
        User user = userRepository.findByUserSeq(request.getUserSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));

        user.setNickname(request.getNickname());
        user.setUpdatedAt(request.getUpdateAt());

        userRepository.save(user);
    }

    // 로그아웃
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Authorization 헤더가 없거나 Bearer 형식이 아닙니다.");
            return ResponseEntity.badRequest().body(null);
        }
        String token = authHeader.substring(7);
        long remainingTime;
        try {
            remainingTime = jwtTokenProvider.getRemainingTime(token);
        } catch (Exception e) {
            log.error("JWT 파싱 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
        try {
            redisService.saveRemoveToken(token, remainingTime);
        } catch (Exception e) {
            log.error("Redis 처리 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
        return ResponseEntity.ok().build();
    }

    // 회원탈퇴
    @PostMapping("/auth/delete")
    public ResponseEntity<String> deleteUser(@RequestParam Integer userSeq) {
        userService.deleteUser(userSeq);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}
