package com.umc.EveryWear.domain.review.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviewPhoto")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReviewPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_photo_id")
    private Long reviewPhotoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "photo_url")
    private String photoUrl;
}
