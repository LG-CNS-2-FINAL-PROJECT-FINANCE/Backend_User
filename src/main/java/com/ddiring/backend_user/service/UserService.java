package com.ddiring.backend_user.service;

import com.ddiring.backend_user.dto.request.UserSignUpRequest;
import com.ddiring.backend_user.entity.User;
import com.ddiring.backend_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 회원가입
    @Transactional
    public void registerUser(UserSignUpRequest request) {

        Integer age = updateAge(request.getBirthDate());
        User user = User.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .kakaoId(request.getKakaoId())
                .nickname(request.getNickname())
                .gender(request.getGender())
                .role(request.getRole())
                .birthDate(request.getBirthDate())
                .age(age)
                .createdAt(LocalDateTime.now())
                .createdId(0)
                .updatedAt(LocalDateTime.now())
                .updatedId(0)
                .user_status(User.UserStatus.ACTIVE)
                .latestAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    // 회원탈퇴
    @Transactional
    public void deleteUser(Integer userSeq) {
        User user = userRepository.findByUserSeq(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.updateUserStatus(User.UserStatus.DELETED);
        user.updateUpdatedInfo(0);
    }

    // 나이 계산
    public Integer updateAge(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("생년월일은 필수 입력 항목입니다.");
        }

        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
