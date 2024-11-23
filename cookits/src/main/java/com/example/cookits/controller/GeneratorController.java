package com.example.cookits.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.cookits.dto.RecipeGenerationRequest;
import com.example.cookits.dto.RecipeResponse;
import com.example.cookits.service.RecipeService;

@Controller
@RequestMapping("/generator")
public class GeneratorController {
    
    private final RecipeService recipeService;

    @Autowired // opcional en constructor único
    public GeneratorController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public String showGenerator() {
        return "recipes/generator/form";  // Nueva ruta
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<?> generateRecipe(@RequestBody RecipeGenerationRequest request) {
        try {
            RecipeResponse recipe = recipeService.generateRecipe(request.getPrompt()).get();
            if (recipe.isSuccess()) {
                return ResponseEntity.ok(recipe);
            } else {
                return ResponseEntity.badRequest().body(recipe);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                RecipeResponse.builder()
                    .success(false)
                    .error("Error al generar la receta: " + e.getMessage())
                    .build()
            );
        }
    }

    @GetMapping("/result")
    public String showRecipe(@RequestParam String prompt, Model model) {
        try {
            RecipeResponse recipe = recipeService.generateRecipe(prompt).get();
            model.addAttribute("recipe", recipe);
            return "recipes/generator/result";  // Nueva ruta
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}