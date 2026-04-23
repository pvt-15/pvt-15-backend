package com.example.accessingdatamysql.model.challenge.entity;

import com.example.accessingdatamysql.model.challenge.enums.ChallengeDifficulty;
import com.example.accessingdatamysql.model.challenge.enums.ChallengeType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ChallengeType type;

    @Enumerated(EnumType.STRING)
    private ChallengeDifficulty difficulty;

    private Integer rewardPoints;
    private boolean active;

    private Integer startMonth;
    private Integer endMonth;

    private String locationName;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    private List<ChallengeTask> tasks = new ArrayList<>();
}
