package com.umc.EveryWear.domain.user.repository;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.entity.UserImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserImgRepository extends JpaRepository<UserImg, Long> {

    long countByUser_UserId(Long userId);

    Optional<UserImg> findByUser_UserIdAndRepresentativeTrue(Long userId);

    List<UserImg> findAllByUser_UserId(Long userId);

    Optional<UserImg> findByUserAndRepresentativeTrue(User user);

    Optional<UserImg> findByUser_UserIdAndProfileImageId(Long userId, Long imageId);
    List<UserImg> findAllByUser(User user);

    void deleteAllByUser_UserId(Long userId);

    Optional<UserImg> findByUser_UserIdAndProfileImageId(Long userId, Long imageId);
}
