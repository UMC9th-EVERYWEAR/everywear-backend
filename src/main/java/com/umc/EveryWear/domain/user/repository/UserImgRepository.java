package com.umc.EveryWear.domain.user.repository;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.entity.UserImg;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserImgRepository extends JpaRepository<UserImg, Long> {

    long countByUser_UserId(Long userId);

    Optional<UserImg> findByUser_UserIdAndRepresentativeTrue(Long userId);

    List<UserImg> findAllByUser_UserId(Long userId);

    Optional<UserImg> findByUser_UserIdAndProfileImageId(Long userId, Long imageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ui from UserImg ui where ui.user.userId = :userId")
    List<UserImg> lockAllByUserId(@Param("userId") Long userId);

    boolean existsByUser_UserIdAndRepresentativeTrue(Long userId);
}
