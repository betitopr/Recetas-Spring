package com.example.cookits.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.cookits.model.ForumRecipe;
import com.example.cookits.model.User;
import com.example.cookits.service.ForumRecipeService;

@Controller
@RequestMapping("/recipes")
public class ForumRecipeController {
    private final ForumRecipeService recipeService;

    public ForumRecipeController(ForumRecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public String listRecipes(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable,
            Model model) {
        Page<ForumRecipe> recipes = search != null ?
            recipeService.searchRecipes(search, pageable) :
            recipeService.getAllRecipes(pageable);
        
        model.addAttribute("recipes", recipes);
        model.addAttribute("search", search);
        return "recipes/list";
    }

    @GetMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String showCreateForm(Model model) {
        model.addAttribute("recipe", new ForumRecipe());
        return "recipes/create";
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String createRecipe(
            @ModelAttribute ForumRecipe recipe,
            @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes) {
        try {
            ForumRecipe savedRecipe = recipeService.createRecipe(recipe, image, user);
            redirectAttributes.addFlashAttribute("message", "Receta creada exitosamente");
            return "redirect:/recipes/" + savedRecipe.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la receta: " + e.getMessage());
            return "redirect:/recipes/create";
        }
    }

    @GetMapping("/{id}")
    public String recipeDetail(@PathVariable Long id, Model model) {
        try {
            ForumRecipe recipe = recipeService.getRecipeById(id)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));
            model.addAttribute("recipe", recipe);
            return "recipes/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Receta no encontrada");
            return "error";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        ForumRecipe recipe = recipeService.getRecipeById(id)
            .orElseThrow(() -> new RuntimeException("Receta no encontrada"));
            
        if (!recipe.getAuthor().getId().equals(user.getId())) {
            return "error";
        }
        
        model.addAttribute("recipe", recipe);
        return "recipes/edit";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String updateRecipe(
            @PathVariable Long id,
            @ModelAttribute ForumRecipe recipe,
            @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes) {
        try {
            recipeService.updateRecipe(id, recipe, image, user);
            redirectAttributes.addFlashAttribute("message", "Receta actualizada exitosamente");
            return "redirect:/recipes/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la receta: " + e.getMessage());
            return "redirect:/recipes/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteRecipe(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes) {
        try {
            recipeService.deleteRecipe(id, user);
            redirectAttributes.addFlashAttribute("message", "Receta eliminada exitosamente");
            return "redirect:/recipes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la receta: " + e.getMessage());
            return "redirect:/recipes/" + id;
        }
    }
}