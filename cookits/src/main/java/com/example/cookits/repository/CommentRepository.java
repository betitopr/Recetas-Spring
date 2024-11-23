package com.example.cookits.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cookits.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);
}