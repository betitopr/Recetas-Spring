async function generateRecipe() {
    const prompt = document.getElementById('recipePrompt').value;
    if (!prompt.trim()) {
        alert('Por favor, ingresa una descripción de la receta que deseas');
        return;
    }

    try {
        // Mostrar estado de carga
        const resultDiv = document.getElementById('recipeResult');
        resultDiv.innerHTML = '<div class="loading">Generando receta...</div>';

        const response = await fetch('/generator/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ prompt: prompt })
        });
        
        const recipe = await response.json();
        
        if (!recipe.success) {
            throw new Error(recipe.error || 'Error al generar la receta');
        }

        displayRecipe(recipe);
    } catch (error) {
        console.error('Error:', error);
        const resultDiv = document.getElementById('recipeResult');
        resultDiv.innerHTML = `
            <div class="error-message">
                <h3>Error</h3>
                <p>${error.message}</p>
            </div>
        `;
    }
}

function displayRecipe(recipe) {
    const resultDiv = document.getElementById('recipeResult');
    resultDiv.innerHTML = `
        <div class="recipe-card">
            <h2 class="recipe-title">${recipe.title}</h2>
            <p class="recipe-description">${recipe.description}</p>
            
            <div class="recipe-content">
                <h3>Ingredientes:</h3>
                <div class="ingredients-list">
                    ${recipe.ingredients.split('\n').map(ingredient => 
                        `<div class="ingredient">${ingredient}</div>`
                    ).join('')}
                </div>

                <h3>Instrucciones:</h3>
                <div class="instructions-list">
                    ${recipe.instructions.split('\n').map(instruction => 
                        `<div class="instruction">${instruction}</div>`
                    ).join('')}
                </div>
            </div>
        </div>
    `;
}