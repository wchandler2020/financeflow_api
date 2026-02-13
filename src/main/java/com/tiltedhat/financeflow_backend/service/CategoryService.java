package com.tiltedhat.financeflow_backend.service;

import com.tiltedhat.financeflow_backend.dto.CategoryMapper;
import com.tiltedhat.financeflow_backend.dto.CategoryResponse;
import com.tiltedhat.financeflow_backend.dto.CreateCategoryRequest;
import com.tiltedhat.financeflow_backend.dto.UpdateCategoryRequest;
import com.tiltedhat.financeflow_backend.entity.Category;
import com.tiltedhat.financeflow_backend.entity.CategoryType;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Get all categories available to user (system + custom)
     */
    public List<CategoryResponse> getAllCategories(User user) {
        return categoryRepository.findAllAvailableForUser(user)
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get categories by type (INCOME or EXPENSE)
     */
    public List<CategoryResponse> getCategoriesByType(User user, CategoryType type) {
        return categoryRepository.findByTypeForUser(user, type)
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create custom category for user
     */
    @Transactional
    public CategoryResponse createCategory(User user, CreateCategoryRequest request) {
        // Check if category name already exists for this user
        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new RuntimeException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = categoryMapper.toEntity(request);
        category.setUser(user);
        category.setIsSystem(false);

        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    /**
     * Update custom category (only user's own categories, not system ones)
     */
    @Transactional
    public CategoryResponse updateCategory(User user, Long categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new RuntimeException("Category not found or you don't have permission"));

        if (category.getIsSystem()) {
            throw new RuntimeException("Cannot modify system categories");
        }

        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());

        Category updated = categoryRepository.save(category);
        return categoryMapper.toResponse(updated);
    }

    /**
     * Delete custom category (only user's own categories, not system ones)
     */
    @Transactional
    public void deleteCategory(User user, Long categoryId) {
        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new RuntimeException("Category not found or you don't have permission"));

        if (category.getIsSystem()) {
            throw new RuntimeException("Cannot delete system categories");
        }

        // TODO: Check if category has transactions before deleting
        // For now, we'll just delete it
        categoryRepository.delete(category);
    }

    /**
     * Get single category by ID
     */
    public CategoryResponse getCategoryById(User user, Long categoryId) {
        // User can view system categories or their own custom categories
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Check permission: must be system category OR user's category
        if (!category.getIsSystem() && !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to view this category");
        }

        return categoryMapper.toResponse(category);
    }
}
