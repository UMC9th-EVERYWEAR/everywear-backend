package com.umc.EveryWear.domain.user.repository;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.enums.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthIdAndSocialType(String oauthId, SocialType socialType);
    Optional<User> findByEmail(String email);
}