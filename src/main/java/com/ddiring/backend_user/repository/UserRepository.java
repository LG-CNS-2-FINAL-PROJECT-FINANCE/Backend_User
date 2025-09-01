package com.ddiring.backend_user.repository;

import com.ddiring.backend_user.entity.User;
import com.ddiring.backend_user.entity.User.UserStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserSeq(String userSeq);

    Optional<User> findByAdminId(String adminId);

    boolean existsByAdminId(String adminId);

    boolean existsByEmail(String email);

    List<User> findByUserSeqIn(List<String> userInfo);

    Optional<User> findByEmail(String email);

    List<User> findByEmailContainingIgnoreCase(String emailPart);

    List<User> findByNicknameContainingIgnoreCase(String nicknamePart);

    List<User> findByUserStatusAndUpdatedAtBefore(UserStatus status, LocalDateTime before);
}