package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.PictureCategory;
import com.example.accessingdatamysql.picture.entity.UserDiscovery;
import org.springframework.stereotype.Service;

@Service
public class DiscoveryService {

    private static final int NEW_UNIQUE_POINTS = 5;
    private static final int MILESTONE_BONUS = 20;

    private final UserDiscoveryRepository userDiscoveryRepository;

    public DiscoveryService(UserDiscoveryRepository userDiscoveryRepository){
        this.userDiscoveryRepository = userDiscoveryRepository;
    }

    public int awardCollectionPoints(User user, PictureCategory pictureCategory, String label){
        String normalizedLabel = normalizeLabel(label);

        boolean alreadyExists = userDiscoveryRepository.existsByUserAndCategoryAndNormalizedLabel
    }

}
