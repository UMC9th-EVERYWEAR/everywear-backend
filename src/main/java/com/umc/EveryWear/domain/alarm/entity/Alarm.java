package com.umc.EveryWear.domain.alarm.entity;

import com.umc.EveryWear.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alarm")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;
}
