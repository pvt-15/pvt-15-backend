package com.example.accessingdatamysql.model.challenge.entity;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class UserChallengeProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Challenge challenge;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
