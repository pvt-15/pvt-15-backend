package com.example.accessingdatamysql.gamification;

import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProgressionService {

    private final UserRepository userRepository;

    public UserProgressionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void applyAward(User user, int pointsToAdd) {
        if (pointsToAdd < 0) {
            throw new IllegalArgumentException("pointsToAdd cannot be negative");
        }

        if (pointsToAdd > 0) {
            user.setTotalPoints(user.getTotalPoints() + pointsToAdd);
        }

        user.setLevel(Level.fromPoints(user.getTotalPoints()));
        userRepository.save(user);
    }
}