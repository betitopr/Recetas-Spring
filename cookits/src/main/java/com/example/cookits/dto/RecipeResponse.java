package com.example.cookits.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeResponse {
    private String title;
    private String description;
    private String ingredients;
    private String instructions;
    private String imageUrl;
    private String error;
    private boolean success;
}