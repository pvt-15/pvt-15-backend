package com.example.accessingdatamysql.model.challenge.entity;

import com.example.accessingdatamysql.picture.entity.Picture;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_challenge_picture_match",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_challenge_task_progress_id", "picture_id"}
                )
        }
)
public class UserChallengePictureMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_challenge_task_progress_id", nullable = false)
    private UserChallengeTaskProgress taskProgress;

    @ManyToOne
    @JoinColumn(name = "picture_id", nullable = false)
    private Picture picture;

    private LocalDateTime matchedAt;

    public UserChallengePictureMatch() {
    }

    public Integer getId() {
        return id;
    }

    public UserChallengeTaskProgress getTaskProgress() {
        return taskProgress;
    }

    public Picture getPicture() {
        return picture;
    }

    public LocalDateTime getMatchedAt() {
        return matchedAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTaskProgress(UserChallengeTaskProgress taskProgress) {
        this.taskProgress = taskProgress;
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
    }

    public void setMatchedAt(LocalDateTime matchedAt) {
        this.matchedAt = matchedAt;
    }
}