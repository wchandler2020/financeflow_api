package com.tiltedhat.financeflow_backend.repository;

import com.tiltedhat.financeflow_backend.entity.Category;
import com.tiltedhat.financeflow_backend.entity.CategoryType;
import com.tiltedhat.financeflow_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Find all system categories (available to everyone)
    List<Category> findByIsSystemTrue();

    // Find user's custom categories
    List<Category> findByUser(User user);

    // Find all categories for a user (system + custom)
    @Query("SELECT c FROM Category c WHERE c.isSystem = true OR c.user = :user")
    List<Category> findAllAvailableForUser(User user);

    // Find by type (INCOME or EXPENSE) for a user
    @Query("SELECT c FROM Category c WHERE (c.isSystem = true OR c.user = :user) AND c.type = :type")
    List<Category> findByTypeForUser(User user, CategoryType type);

    // Find specific category for user (to check ownership before update/delete)
    Optional<Category> findByIdAndUser(Long id, User user);

    // Check if category name already exists for user
    boolean existsByNameAndUser(String name, User user);
}
