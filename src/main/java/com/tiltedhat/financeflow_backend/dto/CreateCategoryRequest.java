package com.tiltedhat.financeflow_backend.dto;

import com.tiltedhat.financeflow_backend.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required.")
    @Size(max=50, message = "Category name cannot exceed 50 characters.")
    private String name;

    @NotNull(message = "Category type is required.")
    private CategoryType type;

    @Size(max = 10, message = "Icon cannot exceed 10 characters")
    private String icon;

    @Size(max = 7, message = "Color must be a valid hex code")
    private String color;
}
