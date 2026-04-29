package com.example.accessingdatamysql.achievement.repository;

import com.example.accessingdatamysql.achievement.entity.BadgeDefinition;
import com.example.accessingdatamysql.achievement.entity.UserBadge;
import com.example.accessingdatamysql.user.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserBadgeRepository extends CrudRepository<UserBadge, Integer> {
    boolean existsByUserAndBadgeDefinition(User user, BadgeDefinition badgeDefinition);
    List<UserBadge> findByUser(User user);
}