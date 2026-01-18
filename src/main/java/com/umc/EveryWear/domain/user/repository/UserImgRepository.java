package com.umc.EveryWear.domain.user.repository;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.entity.UserImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserImgRepository extends JpaRepository<UserImg, Long> {
    Optional<UserImg> findByProfileImageIdAndUser(Long userImgId, User user);
}
