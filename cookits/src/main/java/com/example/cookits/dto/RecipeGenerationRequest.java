package com.example.cookits.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RecipeGenerationRequest {
    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(min = 3, max = 500, message = "La descripción debe tener entre 3 y 500 caracteres")
    private String prompt;
}
