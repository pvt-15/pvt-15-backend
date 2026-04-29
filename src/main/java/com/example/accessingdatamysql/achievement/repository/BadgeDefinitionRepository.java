package com.example.accessingdatamysql.achievement.repository;

import com.example.accessingdatamysql.achievement.entity.BadgeDefinition;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BadgeDefinitionRepository extends CrudRepository<BadgeDefinition, Integer> {
    List<BadgeDefinition> findByActiveTrueAndCategory(PictureCategory category);
}