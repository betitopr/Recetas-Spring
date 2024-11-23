package com.example.cookits.service;

import com.example.cookits.model.ForumRecipe;
import com.example.cookits.model.User;
import com.example.cookits.repository.ForumRecipeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@Transactional
public class ForumRecipeService {
    
    private final ForumRecipeRepository recipeRepository;
    private final FileStorageService fileStorageService;

    public ForumRecipeService(ForumRecipeRepository recipeRepository, 
                            FileStorageService fileStorageService) {
        this.recipeRepository = recipeRepository;
        this.fileStorageService = fileStorageService;
    }

    public Page<ForumRecipe> getAllRecipes(Pageable pageable) {
        return recipeRepository.findAll(pageable);
    }

    public Page<ForumRecipe> searchRecipes(String query, Pageable pageable) {
        return recipeRepository.findByTitleContainingIgnoreCase(query, pageable);
    }

    public Optional<ForumRecipe> getRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    public ForumRecipe createRecipe(ForumRecipe recipe, MultipartFile image, User author) {
        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(image);
            recipe.setImageUrl(imageUrl);
        }
        recipe.setAuthor(author);
        return recipeRepository.save(recipe);
    }

    public ForumRecipe updateRecipe(Long id, ForumRecipe updatedRecipe, MultipartFile image, User currentUser) {
        ForumRecipe existingRecipe = recipeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        if (!existingRecipe.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permiso para editar esta receta");
        }

        existingRecipe.setTitle(updatedRecipe.getTitle());
        existingRecipe.setDescription(updatedRecipe.getDescription());
        existingRecipe.setIngredients(updatedRecipe.getIngredients());
        existingRecipe.setInstructions(updatedRecipe.getInstructions());

        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(image);
            existingRecipe.setImageUrl(imageUrl);
        }

        return recipeRepository.save(existingRecipe);
    }

    public void deleteRecipe(Long id, User currentUser) {
        ForumRecipe recipe = recipeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        if (!recipe.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar esta receta");
        }

        recipeRepository.delete(recipe);
    }

    public Page<ForumRecipe> getRecipesByAuthor(User author, Pageable pageable) {
        return recipeRepository.findByAuthorId(author.getId(), pageable);
    }
}