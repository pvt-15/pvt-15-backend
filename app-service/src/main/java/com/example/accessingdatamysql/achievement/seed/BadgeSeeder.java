package com.example.accessingdatamysql.achievement.seed;

import com.example.accessingdatamysql.achievement.entity.BadgeDefinition;
import com.example.accessingdatamysql.achievement.enums.BadgeTier;
import com.example.accessingdatamysql.achievement.repository.BadgeDefinitionRepository;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BadgeSeeder implements CommandLineRunner {

    private final BadgeDefinitionRepository badgeDefinitionRepository;

    public BadgeSeeder(BadgeDefinitionRepository badgeDefinitionRepository) {
        this.badgeDefinitionRepository = badgeDefinitionRepository;
    }

    @Override
    public void run(String... args) {
        if (badgeDefinitionRepository.count() > 0) {
            return;
        }

        seedCategoryBadges(PictureCategory.FLOWER, "Flower");
        seedCategoryBadges(PictureCategory.TREE, "Tree");
        seedCategoryBadges(PictureCategory.INSECT, "Insect");
        seedCategoryBadges(PictureCategory.BIRD, "Bird");
        seedCategoryBadges(PictureCategory.ANIMAL, "Animal");
        seedCategoryBadges(PictureCategory.PLANT, "Plant");
    }

    private void seedCategoryBadges(PictureCategory category, String displayName) {
        saveBadge(
                category.name() + "_BRONZE",
                displayName + " Bronze Badge",
                "Find 3 unique " + displayName.toLowerCase() + " discoveries.",
                category,
                BadgeTier.BRONZE,
                3
        );

        saveBadge(
                category.name() + "_SILVER",
                displayName + " Silver Badge",
                "Find 6 unique " + displayName.toLowerCase() + " discoveries.",
                category,
                BadgeTier.SILVER,
                6
        );

        saveBadge(
                category.name() + "_GOLD",
                displayName + " Gold Badge",
                "Find 9 unique " + displayName.toLowerCase() + " discoveries.",
                category,
                BadgeTier.GOLD,
                9
        );

        saveBadge(
                category.name() + "_PLATINUM",
                displayName + " Platinum Badge",
                "Find 12 unique " + displayName.toLowerCase() + " discoveries.",
                category,
                BadgeTier.PLATINUM,
                12
        );
    }

    private void saveBadge(String code,
                           String name,
                           String description,
                           PictureCategory category,
                           BadgeTier tier,
                           int requiredCount) {
        BadgeDefinition badgeDefinition = new BadgeDefinition();
        badgeDefinition.setCode(code);
        badgeDefinition.setName(name);
        badgeDefinition.setDescription(description);
        badgeDefinition.setCategory(category);
        badgeDefinition.setTier(tier);
        badgeDefinition.setRequiredCount(requiredCount);
        badgeDefinition.setActive(true);

        badgeDefinitionRepository.save(badgeDefinition);
    }
}