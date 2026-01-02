package com.umc.EveryWear.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "userImg")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserImg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_image_id")
    private Long profileImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;
}
