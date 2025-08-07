package com.ddiring.backend_user.controller;

import com.ddiring.backend_user.dto.request.UserEditRequest;
import com.ddiring.backend_user.dto.request.UserSignUpRequest;
import com.ddiring.backend_user.dto.response.UserInfoResponse;
import com.ddiring.backend_user.entity.User;
import com.ddiring.backend_user.kakao.KakaoOAuthService;
import com.ddiring.backend_user.repository.UserRepository;
import com.ddiring.backend_user.secret.jwt.JwtTokenProvider;
import com.ddiring.backend_user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/user/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final KakaoOAuthService kakaoOAuthService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    // 회원가입
    @PostMapping(value = "/signup")
    public ResponseEntity<String> registerUser(@RequestBody @Valid UserSignUpRequest request) {
        if (request.getRole() == User.Role.ADMIN) {
            return ResponseEntity.badRequest().body("관리자 등록 불가");
        }
        userService.registerUser(request);

        return ResponseEntity.ok("회원 등록이 완료되었습니다.");
    }

    // 회원 로그인
    @PostMapping("/login")
    public ResponseEntity<?> kakaoLogin(
            @RequestParam("code") String code,
            @RequestParam("role") String roleStr,
            @RequestParam("gender") String genderStr,
            @RequestParam("birthDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate
    ) {
        String accessToken = kakaoOAuthService.getAccessToken(code);
        KakaoOAuthService.KakaoUserInfo userInfo = kakaoOAuthService.getUserInfo(accessToken);

        User.Role role;
        try {
            role = User.Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("역할을 지정해주세요.");
        }

        User.Gender gender;
        try {
            gender = User.Gender.valueOf(genderStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("성별을 입력해주세요.");
        }

        Optional<User> optionalUser = userRepository.findByKakaoId(userInfo.getKakaoId());

        User user =optionalUser.orElseGet(() -> {
            return userRepository.save(User.builder()
                    .kakaoId(userInfo.getKakaoId())
                    .email(userInfo.getEmail())
                    .role(role)
                    .nickname("kakao_" +userInfo.getKakaoId())
                    .gender(gender)
                    .birthDate(birthDate)
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .createdId(0)
                    .updatedId(0)
                    .latestAt(LocalDate.now())
                    .user_status(User.UserStatus.ACTIVE)
                    .build());
        });

        String token = jwtTokenProvider.createToken(user);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer" + token)
                .body("로그인 성공");
    }

    // 관리자 회원가입
    @PostMapping("/admin/signup")
    public ResponseEntity<?> registerAdmin(
            @RequestParam String adminId,
            @RequestParam String password
    ) {
        if (userRepository.existsByAdminId(adminId)) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }

        User admin = User.builder()
                .adminId(adminId)
                .password(passwordEncoder.encode(rawPassword))
                .role(User.Role.ADMIN)
                .build();

        userRepository.save(admin);

        return ResponseEntity.ok("관리자 등록 완료");
    }

    // 관리자 로그인
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(
            @RequestParam("userSeq") String adminId,
            @RequestParam("password") String password
    ) {
        Optional<User> admin = userRepository.findByAdminId(adminId);

        if (admin.isEmpty()) {
            return ResponseEntity.badRequest().body("존재하지 않는 계정입니다.");
        }

        User user = admin.get();

        if (user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.badRequest().body("관리자 권한이 없습니다.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("아이디 또는 비밀번호가 틀렸습니다.");
        }

        String token = jwtTokenProvider.AdminCreateToken(user);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer" + token)
                .body("관리자 로그인 성공");
    }

    // 개인 정보 조회
    @GetMapping("/{userSeq}")
    public UserInfoResponse getUserInfo(@PathVariable Integer userSeq) {
        User user = userRepository.findByUserSeq(userSeq)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));

        return new UserInfoResponse(
                user.getEmail(),
                user.getNickname(),
                user.getGender(),
                user.getBirthDate()
        );
    }

    // 회원 정보 수정
    @PostMapping("/edit")
    public void editUser(@RequestBody UserEditRequest request) {
        User user = userRepository.findByUserSeq(request.getUserSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));

        user.setNickname(request.getNickname());
        user.setUpdatedAt(request.getUpdateAt());

        userRepository.save(user);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("로그아웃 완료");
    }

    // 회원탈퇴
    @PostMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestParam Integer userSeq) {
        userService.deleteUser(userSeq);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}
