package com.example.baseproject3_foodrecipe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.model.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class RecipeViewModel : ViewModel() {
    private val repository = RecipeRepository()

    private val _featuredRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val featuredRecipes: StateFlow<List<Recipe>> = _featuredRecipes.asStateFlow()

    private val _popularRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val popularRecipes: StateFlow<List<Recipe>> = _popularRecipes.asStateFlow()

    private val _recipesByCategory = MutableStateFlow<Map<String, List<Recipe>>>(emptyMap())
    val recipesByCategory: StateFlow<Map<String, List<Recipe>>> = _recipesByCategory.asStateFlow()

    private val _currentRecipe = MutableStateFlow<Recipe?>(null)
    val currentRecipe: StateFlow<Recipe?> = _currentRecipe.asStateFlow()

    private val _savedRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val savedRecipes: StateFlow<List<Recipe>> = _savedRecipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadAllRecipes()
    }

    fun loadAllRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allRecipes = repository.getRecipes()

                // Only show recipes with real users
                val validRecipes = allRecipes.filter { recipe ->
                    recipe.authorId.isNotBlank() && recipe.authorName.isNotBlank()
                }

                // Featured recipes (newest)
                _featuredRecipes.value = validRecipes.take(6)

                // Popular recipes (highest rated)
                _popularRecipes.value = validRecipes.sortedByDescending { it.rating }.take(6)

                // Recipes by category
                val categorized = mutableMapOf<String, MutableList<Recipe>>()
                validRecipes.forEach { recipe ->
                    recipe.categories.forEach { category ->
                        if (!categorized.containsKey(category)) {
                            categorized[category] = mutableListOf()
                        }
                        categorized[category]?.add(recipe)
                    }
                }
                _recipesByCategory.value = categorized

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getRecipeById(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipe = repository.getRecipe(recipeId)
                _currentRecipe.value = recipe
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getRecipesByCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipes = repository.getRecipesByCategory(category)
                _recipesByCategory.value = mapOf(category to recipes)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getSavedRecipes(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipes = repository.getSavedRecipes(userId)
                _savedRecipes.value = recipes
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load saved recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createRecipe(
        name: String,
        description: String,
        imageUrl: String,
        authorId: String,
        authorName: String,
        prepTime: Int,
        cookTime: Int,
        servings: Int,
        difficulty: String,
        ingredients: List<String>,
        instructions: List<String>,
        categories: List<String>,
        calories: Int = 0
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipe = Recipe(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    imageUrl = imageUrl,
                    authorId = authorId,
                    authorName = authorName,
                    prepTime = prepTime,
                    cookTime = cookTime,
                    totalTime = prepTime + cookTime,
                    servings = servings,
                    difficulty = difficulty,
                    ingredients = ingredients,
                    instructions = instructions,
                    categories = categories,
                    calories = calories,
                    cuisine = if (categories.contains("Italian")) "Italian"
                    else if (categories.contains("Asian")) "Asian"
                    else if (categories.contains("Mexican")) "Mexican"
                    else ""
                )

                repository.createRecipe(recipe)
                _errorMessage.value = null

                // Reload recipes to update the UI
                loadAllRecipes()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteRecipe(recipeId)
                _errorMessage.value = null

                // Reload recipes to update the UI
                loadAllRecipes()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun bookmarkRecipe(recipeId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.bookmarkRecipe(recipeId, userId)
                // Refresh saved recipes
                getSavedRecipes(userId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to bookmark recipe: ${e.message}"
            }
        }
    }

    fun unbookmarkRecipe(recipeId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.unbookmarkRecipe(recipeId, userId)
                // Refresh saved recipes
                getSavedRecipes(userId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove bookmark: ${e.message}"
            }
        }
    }

    suspend fun isRecipeSaved(recipeId: String, userId: String): Boolean {
        return try {
            repository.isRecipeSaved(recipeId, userId)
        } catch (e: Exception) {
            _errorMessage.value = "Error checking if recipe is saved: ${e.message}"
            false
        }
    }
}
