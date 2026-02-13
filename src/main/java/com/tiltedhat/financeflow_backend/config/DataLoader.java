package com.tiltedhat.financeflow_backend.config;

import com.tiltedhat.financeflow_backend.entity.Category;
import com.tiltedhat.financeflow_backend.entity.CategoryType;
import com.tiltedhat.financeflow_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        // Only seed if no system categories exist
        if (categoryRepository.findByIsSystemTrue().isEmpty()) {
            seedSystemCategories();
        }
    }

    private void seedSystemCategories() {
        List<Category> systemCategories = Arrays.asList(
                // EXPENSE categories
                createSystemCategory("Groceries", CategoryType.EXPENSE, "🛒", "#4CAF50"),
                createSystemCategory("Dining Out", CategoryType.EXPENSE, "🍕", "#FF5722"),
                createSystemCategory("Transportation", CategoryType.EXPENSE, "🚗", "#2196F3"),
                createSystemCategory("Entertainment", CategoryType.EXPENSE, "🎬", "#9C27B0"),
                createSystemCategory("Shopping", CategoryType.EXPENSE, "🛍️", "#E91E63"),
                createSystemCategory("Bills & Utilities", CategoryType.EXPENSE, "💡", "#FF9800"),
                createSystemCategory("Healthcare", CategoryType.EXPENSE, "🏥", "#00BCD4"),
                createSystemCategory("Education", CategoryType.EXPENSE, "📚", "#3F51B5"),
                createSystemCategory("Travel", CategoryType.EXPENSE, "✈️", "#009688"),
                createSystemCategory("Other Expenses", CategoryType.EXPENSE, "📌", "#607D8B"),

                // INCOME categories
                createSystemCategory("Salary", CategoryType.INCOME, "💼", "#4CAF50"),
                createSystemCategory("Freelance", CategoryType.INCOME, "💻", "#8BC34A"),
                createSystemCategory("Investments", CategoryType.INCOME, "📈", "#CDDC39"),
                createSystemCategory("Other Income", CategoryType.INCOME, "💰", "#FFC107")
        );

        categoryRepository.saveAll(systemCategories);
        System.out.println("✅ Seeded " + systemCategories.size() + " system categories");
    }

    private Category createSystemCategory(String name, CategoryType type, String icon, String color) {
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setIcon(icon);
        category.setColor(color);
        category.setIsSystem(true);
        category.setUser(null);  // System categories don't belong to a user
        return category;
    }
}
