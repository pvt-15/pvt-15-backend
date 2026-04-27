package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.PictureCategory;
import com.example.accessingdatamysql.picture.entity.UserDiscovery;
import com.example.accessingdatamysql.picture.repository.UserDiscoveryRepository;
import com.example.accessingdatamysql.picture.dto.DiscoveryCategoryStatsResponse;
import com.example.accessingdatamysql.picture.dto.DiscoveryStatsResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiscoveryService {

    private static final int NEW_UNIQUE_POINTS = 5;
    private static final int MILESTONE_BONUS = 20;

    private final UserDiscoveryRepository userDiscoveryRepository;

    public DiscoveryService(UserDiscoveryRepository userDiscoveryRepository){
        this.userDiscoveryRepository = userDiscoveryRepository;
    }

    public int awardCollectionPoints(User user, PictureCategory pictureCategory, String label){
        String normalizedLabel = normalize(label);

        boolean alreadyExists =
                userDiscoveryRepository.existsByUserAndCategoryAndNormalizedLabel(user, pictureCategory, normalizedLabel);

        if(alreadyExists){
            return 0;
        }

        UserDiscovery discovery = new UserDiscovery();
        discovery.setUser(user);
        discovery.setCategory(pictureCategory);
        discovery.setNormalizedLabel(normalizedLabel);
        discovery.setDiscoveredAt(LocalDateTime.now());
        userDiscoveryRepository.save(discovery);

        long uniqueCountInCategory = userDiscoveryRepository.countByUserAndCategory(user, pictureCategory);

        int points = NEW_UNIQUE_POINTS;

        if(uniqueCountInCategory % 10 == 0){
            points += MILESTONE_BONUS;
        }
        return points;
    }

    public DiscoveryStatsResponse getDiscoveryStats(User user){
        List<DiscoveryCategoryStatsResponse> categories =  new ArrayList<>();

        categories.add(createCategoryStats(user, PictureCategory.FLOWER));
        categories.add(createCategoryStats(user, PictureCategory.TREE));
        categories.add(createCategoryStats(user, PictureCategory.PLANT));
        categories.add(createCategoryStats(user, PictureCategory.ANIMAL));
        categories.add(createCategoryStats(user, PictureCategory.BIRD));
        categories.add(createCategoryStats(user, PictureCategory.INSECT));

        return new DiscoveryStatsResponse(categories);
    }

    private DiscoveryCategoryStatsResponse createCategoryStats(User user, PictureCategory category){
        long uniqueCount = userDiscoveryRepository.countByUserAndCategory(user, category);

        long nextMileStone = ((uniqueCount / 10) * 10);
        long remainingToNextMilestone = nextMileStone - uniqueCount;

        return new DiscoveryCategoryStatsResponse(
                category.name(),
                uniqueCount,
                nextMileStone,
                remainingToNextMilestone
        );
    }


    private String normalize(String label) {
        if (label == null) {
            return "";
        }

        return label.trim().toLowerCase();
    }
}
