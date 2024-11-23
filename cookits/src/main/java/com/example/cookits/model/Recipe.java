package com.example.cookits.model;
import lombok.Data;

@Data
public class Recipe {
    private String title;
    private String description;
    private String ingredients;
    private String instructions;
    private String imageUrl;
}
