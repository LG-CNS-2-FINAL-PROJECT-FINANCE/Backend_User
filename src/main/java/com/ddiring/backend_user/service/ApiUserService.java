package com.ddiring.backend_user.service;

import com.ddiring.backend_user.dto.UserDTO;
import com.ddiring.backend_user.dto.UserNameDto;
import com.ddiring.backend_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiUserService {

    private final UserRepository userRepository;

    public List<UserDTO> getUserInfo(List<String> userInfo) {
        return userRepository.findByUserSeqIn(userInfo).stream()
                .map(user -> UserDTO.builder()
                        .userSeq(user.getUserSeq())
                        .userName(user.getUserName())
                        .nickname(user.getNickname())
                        .build())
                .toList();
    }

    public Optional<UserNameDto> getUserName(String userSeq) {
        return userRepository.findByUserSeq(userSeq, UserNameDto.class);
    }
}
