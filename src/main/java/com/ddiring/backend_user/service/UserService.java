package com.ddiring.backend_user.service;

import com.ddiring.backend_user.dto.request.UserLoginRequest;
import com.ddiring.backend_user.api.ProductDto;
import com.ddiring.backend_user.api.ProductClient;
import com.ddiring.backend_user.dto.request.AdminRequest;
import com.ddiring.backend_user.dto.request.UserAdditionalInfoRequest;
import com.ddiring.backend_user.dto.request.UserEditRequest;
import com.ddiring.backend_user.dto.response.UserInfoResponse;
import com.ddiring.backend_user.dto.response.UserListResponse;
import com.ddiring.backend_user.entity.User;
import com.ddiring.backend_user.entity.User.Role;
import com.ddiring.backend_user.entity.User.UserStatus;
import com.ddiring.backend_user.kakao.KakaoOAuthService;
import com.ddiring.backend_user.repository.UserRepository;
import com.ddiring.backend_user.secret.jwt.JwtTokenProvider;
import com.ddiring.backend_user.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KakaoOAuthService kakaoOAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;
    private final PlatformTransactionManager transactionManager;
    private final ProductClient productClient;

    // 카카오 로그인
    public ResponseEntity<?> kakaoLogin(String code, UserLoginRequest request) {
        String kakaoAccessToken = kakaoOAuthService.getAccessToken(code);
        KakaoOAuthService.KakaoUserInfo userInfo = kakaoOAuthService.getUserInfo(kakaoAccessToken);

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        AtomicBoolean firstLogin = new AtomicBoolean(false);
        User user = tx.execute(status -> {
            User u = userRepository.findByEmail(userInfo.getEmail()).orElse(null);
            boolean hasAdditional = hasAdditionalInfo(request);
            if (u == null) {
                firstLogin.set(true);
                LocalDate birthDate = (hasAdditional) ? request.getBirthDate() : null;
                Integer age = (birthDate != null) ? calculateAge(birthDate) : null;
                u = User.builder()
                        .email(userInfo.getEmail())
                        .userName(hasAdditional ? request.getUserName() : null)
                        .nickname(hasAdditional ? request.getNickname() : null)
                        .gender(hasAdditional ? request.getGender() : null)
                        .birthDate(birthDate)
                        .age(age)
                        .role(Role.GUEST)
                        .latestAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .user_status(User.UserStatus.ACTIVE)
                        .profileCompleted(hasAdditional)
                        .build();
                u = userRepository.save(u);
            } else {
                if (Boolean.FALSE.equals(u.getProfileCompleted()) && hasAdditional) {
                    LocalDate birthDate = request.getBirthDate();
                    Integer age = (birthDate != null) ? calculateAge(birthDate) : null;
                    u.setUserName(request.getUserName());
                    u.setNickname(request.getNickname());
                    u.setGender(request.getGender());
                    u.setBirthDate(birthDate);
                    u.setAge(age);
                    u.setProfileCompleted(true);
                }
                if (u.getProfileCompleted() == null) {
                    boolean completed = u.getUserName() != null
                            && u.getNickname() != null
                            && u.getGender() != null
                            && u.getBirthDate() != null;
                    u.setProfileCompleted(completed);
                }
                u.setLatestAt(LocalDateTime.now());
                u.setUpdatedAt(LocalDateTime.now());
                userRepository.save(u);
            }
            return u;
        });
        String accessToken = jwtTokenProvider.createToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        long accessTtlSec = 0L;
        long refreshTtlSec = 0L;
        try {
            accessTtlSec = jwtTokenProvider.getRemainingTime(accessToken);
        } catch (Exception ignored) {
        }
        try {
            refreshTtlSec = jwtTokenProvider.getRemainingTime(refreshToken);
        } catch (Exception ignored) {
        }
        String accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(accessTtlSec, 0L)).toString();
        String refreshTokenExpiresAt = Instant.now().plusSeconds(Math.max(refreshTtlSec, 0L)).toString();

        boolean completed = user != null && Boolean.TRUE.equals(user.getProfileCompleted());
        String message = completed ? "로그인 성공" : "추가 회원 정보가 필요합니다.";

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)
                .body(java.util.Map.of(
                        "message", message,
                        "accessToken", accessToken,
                        "refreshToken", refreshToken,
                        "accessTokenExpiresAt", accessTokenExpiresAt,
                        "refreshTokenExpiresAt", refreshTokenExpiresAt));
    }

    // 회원 정보 등록
    @Transactional
    public void signUpUser(String userSeq, UserAdditionalInfoRequest request) {
        User user = getUserOrThrow(userSeq);
        user.setUserName(request.getUserName());
        user.setNickname(request.getNickname());
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());
        user.setAge(calculateAge(request.getBirthDate()));
        user.setUser_status(User.UserStatus.ACTIVE);
        user.setProfileCompleted(true);
        userRepository.save(user);
    }

    // 나이 계산
    public int calculateAge(LocalDate birthDate) {
        if (birthDate == null)
            throw new IllegalArgumentException("생년월일은 필수 입력 항목입니다.");
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // 관리자 회원가입
    @Transactional
    public ResponseEntity<?> registerAdmin(AdminRequest request) {
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
                .createdId("system")
                .createdAt(LocalDateTime.now())
                .updatedId("system")
                .updatedAt(LocalDateTime.now())
                .user_status(User.UserStatus.ACTIVE)
                .profileCompleted(true)
                .build();
        userRepository.save(admin);
        return ResponseEntity.ok("관리자 등록 완료");
    }

    // 관리자 로그인
    @Transactional
    public ResponseEntity<?> adminLogin(AdminRequest request) {
        User user = userRepository.findByAdminId(request.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
        if (user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.badRequest().body("관리자 권한이 없습니다.");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("아이디 또는 비밀번호가 틀렸습니다.");
        }
        String accessToken = jwtTokenProvider.adminCreateToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        long accessTtlSec = 0L;
        long refreshTtlSec = 0L;
        try {
            accessTtlSec = jwtTokenProvider.getRemainingTime(accessToken);
        } catch (Exception ignored) {
        }
        try {
            refreshTtlSec = jwtTokenProvider.getRemainingTime(refreshToken);
        } catch (Exception ignored) {
        }
        String accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(accessTtlSec, 0L)).toString();
        String refreshTokenExpiresAt = Instant.now().plusSeconds(Math.max(refreshTtlSec, 0L)).toString();

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)
                .body(java.util.Map.of(
                        "message", "관리자 로그인 성공",
                        "accessToken", accessToken,
                        "refreshToken", refreshToken,
                        "accessTokenExpiresAt", accessTokenExpiresAt,
                        "refreshTokenExpiresAt", refreshTokenExpiresAt));
    }

    // 개인 정보 조회
    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(String userSeq) {
        User user = getUserOrThrow(userSeq);
        return new UserInfoResponse(
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getBirthDate(),
                user.getAge(),
                user.getGender(),
                user.getUser_status(),
                user.getLatestAt());
    }

    // 모든 사용자 정보 조회
    @Transactional(readOnly = true)
    public List<UserListResponse> getUserList() {
        return userRepository.findAll().stream()
                .map(user -> new UserListResponse(
                        user.getUserSeq(),
                        user.getEmail(),
                        user.getNickname(),
                        user.getRole(),
                        user.getAge(),
                        user.getGender(),
                        user.getUser_status(),
                        user.getLatestAt()))
                .collect(Collectors.toList());
    }

    // 사용자 검색
    @Transactional(readOnly = true)
    public List<UserListResponse> searchUsers(String searchType, String search) {
        List<User> users = new ArrayList<>();
        switch (searchType.toLowerCase()) {
            case "email" -> users = userRepository.findByEmailContainingIgnoreCase(search);
            case "nickname" -> users = userRepository.findByNicknameContainingIgnoreCase(search);
        }
        return users.stream()
                .map(user -> new UserListResponse(
                        user.getUserSeq(),
                        user.getEmail(),
                        user.getNickname(),
                        user.getRole(),
                        user.getAge(),
                        user.getGender(),
                        user.getUser_status(),
                        user.getLatestAt()))
                .toList();
    }

    // 회원 정보 수정
    @Transactional
    public void editUser(String userSeq, UserEditRequest request) {
        User user = getUserOrThrow(userSeq);
        user.setNickname(request.getNickname());
        userRepository.save(user);
    }

    // 로그아웃
    public ResponseEntity<Void> logout(String authHeader, String refreshTokenHeader) {
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
        if (remainingTime <= 0) {
            log.info("만료된 토큰 로그아웃 요청: 블랙리스트 처리 생략 후 200 반환");
            return ResponseEntity.ok().build();
        }
        try {
            redisService.saveRemoveToken(token, remainingTime);
            if (refreshTokenHeader != null && refreshTokenHeader.startsWith("Bearer ")) {
                String refreshToken = refreshTokenHeader.substring(7);
                try {
                    long refreshTtl = jwtTokenProvider.getRemainingTime(refreshToken);
                    if (refreshTtl > 0) {
                        redisService.saveRemoveToken(refreshToken, refreshTtl);
                    }
                } catch (Exception ex) {
                    log.warn("Refresh Token 처리 중 오류: {}", ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Redis 처리 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
        return ResponseEntity.ok().build();
    }

    // 회원탈퇴
    @Transactional
    public void deleteUser(String userSeq) {
        User user = getUserOrThrow(userSeq);
        user.updateUserStatus(UserStatus.DELETED);
        user.updateUpdatedInfo("deleteUser");
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // 회원탈퇴 응답용
    @Transactional
    public ResponseEntity<String> deleteUserWithResponse(String userSeq) {
        List<ProductDto> products;
        try {
            products = productClient.getAllProduct();
        } catch (Exception e) {
            log.warn("Product service 호출 실패: {}", e.getMessage());
            products = List.of();
        }

        boolean hasProceeding = products.stream()
                .filter(p -> userSeq.equals(p.getUserSeq()))
                .anyMatch(p -> p.getProjectStatus() != null && !"CLOSED".equalsIgnoreCase(p.getProjectStatus()));
        if (hasProceeding) {
            return ResponseEntity.badRequest().body("진행 중인 프로젝트가 있어 탈퇴할 수 없습니다.");
        }

        deleteUser(userSeq);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void purgeSoftDeletedUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(60);
        List<User> targets = userRepository.findByUserStatusAndDeletedAtBefore(UserStatus.DELETED, threshold);
        if (targets.isEmpty())
            return;
        List<String> ids = targets.stream().map(User::getUserSeq).toList();
        log.info("Hard deleting users count={} ids={}", targets.size(), ids);
        userRepository.deleteAll(targets);
    }

    // 역할 선택
    @Transactional
    public ResponseEntity<?> selectRole(String userSeq, Role role) {
        User user = getUserOrThrow(userSeq);

        Role current = user.getRole();
        if (current == null || current == Role.GUEST) {
            if (role == null || (role != Role.USER && role != Role.CREATOR)) {
                return ResponseEntity.badRequest().body("역할을 선택해주세요. USER | CREATOR");
            }
            user.setRole(role);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return buildRoleChangeResponse(user, "역할이 설정되었습니다. 현재 역할: " + user.getRole());
        }

        if (role == null) {
            user.setRole(current == Role.USER ? Role.CREATOR : Role.USER);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return buildRoleChangeResponse(user, "역할이 변경되었습니다. 현재 역할: " + user.getRole());
        }

        if (current == role) {
            return buildRoleChangeResponse(user, "이미 선택된 역할입니다. 현재 역할: " + user.getRole());
        }

        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return buildRoleChangeResponse(user, "역할이 변경되었습니다. 현재 역할: " + user.getRole());
    }

    // 사용자 상태 변경
    @Transactional
    public User updateUserStatus(String userSeq, User.UserStatus status) {
        User user = getUserOrThrow(userSeq);
        user.updateUserStatus(status);
        user.updateUpdatedInfo("system");
        userRepository.save(user);
        return user;
    }

    // 사용자 상태 변경 응답용
    @Transactional
    public ResponseEntity<String> updateUserStatusWithResponse(String userSeq, User.UserStatus status) {
        User user = updateUserStatus(userSeq, status);
        return ResponseEntity.ok("사용자 상태가 변경되었습니다. 현재 상태: " + user.getUser_status());
    }

    // 공통: 사용자 조회
    private User getUserOrThrow(String userSeq) {
        return userRepository.findByUserSeq(userSeq)
                .orElseThrow(() -> new com.ddiring.backend_user.common.exception.NotFound("해당 사용자를 찾을 수 없습니다."));
    }

    private boolean hasAdditionalInfo(UserLoginRequest request) {
        if (request == null)
            return false;
        return request.getUserName() != null
                && request.getNickname() != null
                && request.getGender() != null
                && request.getBirthDate() != null;
    }

    // 토큰 재발급 응답
    private ResponseEntity<?> buildRoleChangeResponse(User user, String message) {
        String accessToken = jwtTokenProvider.createToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        long accessTtlSec = 0L;
        long refreshTtlSec = 0L;
        try {
            accessTtlSec = jwtTokenProvider.getRemainingTime(accessToken);
        } catch (Exception ignored) {
        }
        try {
            refreshTtlSec = jwtTokenProvider.getRemainingTime(refreshToken);
        } catch (Exception ignored) {
        }
        String accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(accessTtlSec, 0L)).toString();
        String refreshTokenExpiresAt = Instant.now().plusSeconds(Math.max(refreshTtlSec, 0L)).toString();

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)
                .body(java.util.Map.of(
                        "message", message,
                        "accessToken", accessToken,
                        "refreshToken", refreshToken,
                        "accessTokenExpiresAt", accessTokenExpiresAt,
                        "refreshTokenExpiresAt", refreshTokenExpiresAt));
    }
}
