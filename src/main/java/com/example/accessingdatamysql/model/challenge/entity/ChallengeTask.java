package com.example.accessingdatamysql.model.challenge.entity;

import com.example.accessingdatamysql.model.challenge.enums.TaskType;
import com.example.accessingdatamysql.model.enums.PictureCategory;
import jakarta.persistence.*;

@Entity
public class ChallengeTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Challenge challenge;

    private String taskText;

    @Enumerated
    private TaskType taskType;

    private String requiredLabel;

    @Enumerated(EnumType.STRING)
    private PictureCategory requiredCategory;

    private Integer requiredCount;
    private boolean mustBeUnique;
}
