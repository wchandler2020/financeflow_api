package com.tiltedhat.financeflow_backend.dto;

import com.tiltedhat.financeflow_backend.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category toEntity(CreateCategoryRequest request){
        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setIsSystem(false);
        return category;
    }

    public CategoryResponse toResponse(Category category){
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getIcon(),
                category.getColor(),
                category.getIsSystem(),
                category.getCreatedAt()
        );
    }
}
