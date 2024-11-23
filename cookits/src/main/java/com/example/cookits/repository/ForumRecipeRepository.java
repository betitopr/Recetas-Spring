package com.example.cookits.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cookits.model.ForumRecipe;

public interface ForumRecipeRepository extends JpaRepository<ForumRecipe, Long> {
    Page<ForumRecipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<ForumRecipe> findByAuthorId(Long authorId, Pageable pageable);
}
