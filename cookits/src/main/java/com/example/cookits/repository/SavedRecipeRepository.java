package com.example.cookits.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cookits.model.SavedRecipe;

public interface SavedRecipeRepository extends JpaRepository<SavedRecipe, Long> {
    Page<SavedRecipe> findByUserId(Long userId, Pageable pageable);
    Optional<SavedRecipe> findByUserIdAndGeneratedRecipeId(Long userId, Long recipeId);
    Optional<SavedRecipe> findByUserIdAndForumRecipeId(Long userId, Long forumRecipeId);
    boolean existsByUserIdAndGeneratedRecipeId(Long userId, Long recipeId);
    boolean existsByUserIdAndForumRecipeId(Long userId, Long forumRecipeId);
}
