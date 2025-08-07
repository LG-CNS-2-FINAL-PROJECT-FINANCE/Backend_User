package com.ddiring.backend_user.repository;

import com.ddiring.backend_user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUserSeq(Integer userSeq);

    Optional<User> findByKakaoId(String kakaoId);

    boolean existsByNickname(String nickname);
}
