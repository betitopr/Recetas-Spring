package com.example.cookits.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.example.cookits.dto.RecipeResponse;
import com.example.cookits.model.GeneratedRecipe;
import com.example.cookits.model.SavedRecipe;
import com.example.cookits.model.User;
import com.example.cookits.repository.GeneratedRecipeRepository;
import com.example.cookits.repository.SavedRecipeRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class RecipeService {
    
    private final GeneratedRecipeRepository generatedRecipeRepository;
    private final SavedRecipeRepository savedRecipeRepository;
    private final GeminiAIService geminiAIService;

    public CompletableFuture<RecipeResponse> generateRecipe(@Valid @NotBlank String prompt) {
        // Validar y limpiar el prompt
        String cleanPrompt = sanitizePrompt(prompt);
        
        // Verificar longitud mínima
        if (cleanPrompt.length() < 3) {
            CompletableFuture<RecipeResponse> future = new CompletableFuture<>();
            future.complete(RecipeResponse.builder()
                .success(false)
                .error("La descripción es demasiado corta")
                .build());
            return future;
        }
        
        return geminiAIService.generateRecipeAsync(cleanPrompt);
    }
    
    private String sanitizePrompt(String prompt) {
        return prompt.trim()
            .replaceAll("[<>{}\\[\\]]", "") // Eliminar caracteres especiales
            .replaceAll("\\s+", " "); // Normalizar espacios
    }

    @Transactional
    public GeneratedRecipe generateAndSaveRecipe(String prompt, User user) {
        var recipeResponse = geminiAIService.generateRecipeAsync(prompt).join();
        
        if (!recipeResponse.isSuccess()) {
            throw new RuntimeException(recipeResponse.getError());
        }

        GeneratedRecipe recipe = new GeneratedRecipe();
        recipe.setTitle(recipeResponse.getTitle());
        recipe.setDescription(recipeResponse.getDescription());
        recipe.setIngredients(recipeResponse.getIngredients());
        recipe.setInstructions(recipeResponse.getInstructions());
        recipe.setImageUrl(recipeResponse.getImageUrl());
        recipe.setPrompt(prompt);
        recipe.setUser(user);

        return generatedRecipeRepository.save(recipe);
    }

    @Transactional(readOnly = true)
    public Page<GeneratedRecipe> getUserGeneratedRecipes(Long userId, Pageable pageable) {
        return generatedRecipeRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SavedRecipe> getUserSavedRecipes(Long userId, Pageable pageable) {
        return savedRecipeRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public SavedRecipe saveRecipe(GeneratedRecipe recipe, User user) {
        if (savedRecipeRepository.existsByUserIdAndGeneratedRecipeId(user.getId(), recipe.getId())) {
            throw new RuntimeException("Ya guardaste esta receta");
        }

        SavedRecipe savedRecipe = new SavedRecipe();
        savedRecipe.setUser(user);
        savedRecipe.setGeneratedRecipe(recipe);
        
        return savedRecipeRepository.save(savedRecipe);
    }
}