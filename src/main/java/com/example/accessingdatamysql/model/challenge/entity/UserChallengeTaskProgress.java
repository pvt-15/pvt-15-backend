package com.example.accessingdatamysql.model.challenge.entity;

import jakarta.persistence.*;

@Entity
public class UserChallengeTaskProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private UserChallengeProgress userChallengeProgress;

    @ManyToOne
    ChallengeTask challengeTask;

    private Integer currentCount;
    private boolean completed;
}
