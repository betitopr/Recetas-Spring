package com.example.cookits.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cookits.model.GeneratedRecipe;

public interface GeneratedRecipeRepository extends JpaRepository<GeneratedRecipe, Long> {
    Page<GeneratedRecipe> findByUserId(Long userId, Pageable pageable);
    Page<GeneratedRecipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
