package com.tiltedhat.financeflow_backend.dto;

import com.tiltedhat.financeflow_backend.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryType type;
    private String icon;
    private String color;
    private boolean isSystem;
    private LocalDateTime createdAt;

}
