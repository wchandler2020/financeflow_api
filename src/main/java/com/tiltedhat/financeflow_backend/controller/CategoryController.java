package com.tiltedhat.financeflow_backend.controller;

import com.tiltedhat.financeflow_backend.dto.CategoryResponse;
import com.tiltedhat.financeflow_backend.dto.CreateCategoryRequest;
import com.tiltedhat.financeflow_backend.dto.MessageResponse;
import com.tiltedhat.financeflow_backend.dto.UpdateCategoryRequest;
import com.tiltedhat.financeflow_backend.entity.CategoryType;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories
     * Get all categories (system + user's custom)
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal User user
    ) {
        List<CategoryResponse> categories = categoryService.getAllCategories(user);
        return ResponseEntity.ok(categories);
    }

    /**
     * GET /api/categories?type=EXPENSE
     * Get categories by type (INCOME or EXPENSE)
     */
    @GetMapping(params = "type")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByType(
            @AuthenticationPrincipal User user,
            @RequestParam CategoryType type
    ) {
        List<CategoryResponse> categories = categoryService.getCategoriesByType(user, type);
        return ResponseEntity.ok(categories);
    }

    /**
     * GET /api/categories/{id}
     * Get single category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        CategoryResponse category = categoryService.getCategoryById(user, id);
        return ResponseEntity.ok(category);
    }

    /**
     * POST /api/categories
     * Create custom category
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CategoryResponse category = categoryService.createCategory(user, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(category);
    }

    /**
     * PUT /api/categories/{id}
     * Update custom category
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        CategoryResponse category = categoryService.updateCategory(user, id, request);
        return ResponseEntity.ok(category);
    }

    /**
     * DELETE /api/categories/{id}
     * Delete custom category
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteCategory(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        categoryService.deleteCategory(user, id);
        return ResponseEntity.ok(new MessageResponse("Category deleted successfully"));
    }
}
