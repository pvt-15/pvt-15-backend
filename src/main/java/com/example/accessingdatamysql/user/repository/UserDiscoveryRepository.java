package com.example.accessingdatamysql.user.repository;

import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.model.enums.PictureCategory;
import com.example.accessingdatamysql.user.entity.UserDiscovery;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserDiscoveryRepository extends CrudRepository<UserDiscovery, Integer> {

    boolean existsByUserAndCategoryAndNormalizedLabel(User user, PictureCategory category, String normalizedLabel);

    long countByUserAndCategory(User user, PictureCategory category);

    List<UserDiscovery> findByUser(User user);
}