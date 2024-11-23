package com.example.cookits.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.cookits.config.GeminiConfig;
import com.example.cookits.dto.RecipeResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class GeminiAIService {
    
    private static final Logger LOGGER = Logger.getLogger(GeminiAIService.class.getName());
    private static final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent";
    
    @Autowired
    private GeminiConfig geminiConfig;
     // Método auxiliar para formatear ingredientes
     private String formatIngredients(JsonArray ingredientsArray) {
        return StreamSupport.stream(ingredientsArray.spliterator(), false)
            .map(ingredient -> "• " + ingredient.getAsString())
            .collect(Collectors.joining("\n"));
    }

    // Método auxiliar para formatear instrucciones
    private String formatInstructions(JsonArray instructionsArray) {
        StringBuilder instructions = new StringBuilder();
        for (int i = 0; i < instructionsArray.size(); i++) {
            instructions.append(String.format("%d. %s", i + 1, instructionsArray.get(i).getAsString()));
            if (i < instructionsArray.size() - 1) {
                instructions.append("\n");
            }
        }
        return instructions.toString();
    }

    public CompletableFuture<RecipeResponse> generateRecipeAsync(String userPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = String.format("""
                    Actúa como un chef experto y genera una receta basada en la siguiente descripción: "%s"
                    La respuesta debe estar en formato JSON con la siguiente estructura exacta:
                    {
                        "title": "Título de la receta",
                        "description": "Breve descripción de la receta",
                        "ingredients": ["ingrediente1", "ingrediente2"],
                        "instructions": ["paso1", "paso2"]
                    }
                    No incluyas ningún texto adicional, solo el JSON.
                    """, userPrompt);

                String requestBody = gson.toJson(Map.of(
                    "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                    ))
                ));

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + "?key=" + geminiConfig.getApiKey()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Error from Gemini API: " + response.body());
                }

                JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
                String generatedText = responseJson
                    .getAsJsonArray("candidates")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0)
                    .getAsJsonObject()
                    .get("text")
                    .getAsString();

                // Limpiar el texto de posibles caracteres extra
                generatedText = generatedText.trim();
                if (generatedText.startsWith("```json")) {
                    generatedText = generatedText.substring(7);
                }
                if (generatedText.endsWith("```")) {
                    generatedText = generatedText.substring(0, generatedText.length() - 3);
                }
                generatedText = generatedText.trim();

                JsonObject recipeJson = JsonParser.parseString(generatedText).getAsJsonObject();

                // Convertir arrays a strings con formato
                String ingredients = formatList(recipeJson.getAsJsonArray("ingredients"), "• ");
                String instructions = formatList(recipeJson.getAsJsonArray("instructions"), "", true);
                
                return RecipeResponse.builder()
                    .title(recipeJson.get("title").getAsString())
                    .description(recipeJson.get("description").getAsString())
                    .ingredients(ingredients)
                    .instructions(instructions)
                    .imageUrl("/images/default-recipe.jpg")
                    .success(true)
                    .build();
                    
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error generating recipe", e);
                return RecipeResponse.builder()
                    .success(false)
                    .error("Error al generar la receta: " + e.getMessage())
                    .build();
            }
        });
    }

    private String formatList(com.google.gson.JsonArray array, String prefix, boolean... numbered) {
        StringBuilder result = new StringBuilder();
        boolean isNumbered = numbered.length > 0 && numbered[0];
        
        for (int i = 0; i < array.size(); i++) {
            if (i > 0) result.append("\n");
            if (isNumbered) {
                result.append(i + 1).append(". ");
            } else {
                result.append(prefix);
            }
            result.append(array.get(i).getAsString());
        }
        
        return result.toString();
    }
}