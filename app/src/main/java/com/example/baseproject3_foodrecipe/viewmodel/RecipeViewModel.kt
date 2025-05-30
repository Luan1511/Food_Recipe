package com.example.baseproject3_foodrecipe.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.BlogPost
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.model.RecipeRepository
import com.example.baseproject3_foodrecipe.model.User
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.UUID

class RecipeViewModel : ViewModel() {
    private val TAG = "RecipeViewModel"
    private val recipeRepository = RecipeRepository()

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _featuredRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val featuredRecipes: StateFlow<List<Recipe>> = _featuredRecipes.asStateFlow()

    private val _userRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val userRecipes: StateFlow<List<Recipe>> = _userRecipes.asStateFlow()

    private val _popularRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val popularRecipes: StateFlow<List<Recipe>> = _popularRecipes.asStateFlow()

    private val _currentRecipe = MutableStateFlow<Recipe?>(null)
    val currentRecipe: StateFlow<Recipe?> = _currentRecipe.asStateFlow()

    private val _savedRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val savedRecipes: StateFlow<List<Recipe>> = _savedRecipes.asStateFlow()

    private val _categoryRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val categoryRecipes: StateFlow<List<Recipe>> = _categoryRecipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // New state for ingredient-based recipe search
    private val _ingredientRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val ingredientRecipes: StateFlow<List<Recipe>> = _ingredientRecipes.asStateFlow()

    init {
        // Initialize by loading recipes
        loadAllRecipes()
        loadFeaturedRecipes()
        loadPopularRecipes()
    }

    fun getAllRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val blogs = recipeRepository.getAllRecipes()
                _recipes.value = blogs
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load blogs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading all recipes")
                val allRecipes = recipeRepository.getAllRecipes()
                _recipes.value = allRecipes
                Log.d(TAG, "Loaded ${allRecipes.size} recipes")
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error loading all recipes: ${e.message}", e)
                _errorMessage.value = "Failed to load recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserRecipes(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipes = recipeRepository.getRecipesByAuthor(userId)
                _userRecipes.value = recipes
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error loading blogs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFeaturedRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading featured recipes")
                val featured = recipeRepository.getFeaturedRecipes()
                _featuredRecipes.value = featured
                Log.d(TAG, "Loaded ${featured.size} featured recipes")
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error loading featured recipes: ${e.message}")
                _errorMessage.value = "Failed to load featured recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load popular recipes
     */
    fun loadPopularRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading popular recipes")
                val popular = recipeRepository.getPopularRecipes()
                _popularRecipes.value = popular
                Log.d(TAG, "Loaded ${popular.size} popular recipes")
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error loading popular recipes: ${e.message}")
                _errorMessage.value = "Failed to load popular recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load saved recipes for a user
     */
    fun loadSavedRecipes(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading saved recipes for user: $userId")
                val saved = recipeRepository.getSavedRecipes(userId)
                _savedRecipes.value = saved
                Log.d(TAG, "Loaded ${saved.size} saved recipes")
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error loading saved recipes: ${e.message}")
                _errorMessage.value = "Failed to load saved recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get recipes by category
     */
    fun getRecipesByCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading recipes by category: $category")
                val recipes = recipeRepository.getRecipesByCategory(category)
                _categoryRecipes.value = recipes
                Log.d(TAG, "Loaded ${recipes.size} recipes for category: $category")
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recipes by category: ${e.message}")
                _errorMessage.value = "Failed to get recipes by category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get recipes by cuisine
     */
    fun getRecipesByCuisine(cuisine: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading recipes by cuisine: $cuisine")
                val recipes = recipeRepository.getRecipesByCuisine(cuisine)
                _categoryRecipes.value = recipes
                Log.d(TAG, "Loaded ${recipes.size} recipes for cuisine: $cuisine")
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recipes by cuisine: ${e.message}")
                _errorMessage.value = "Failed to get recipes by cuisine: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getRecipeById(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading recipe with ID: $recipeId")
                val recipe = recipeRepository.getRecipeById(recipeId)
                _currentRecipe.value = recipe
                Log.d(TAG, recipe?.let { "Loaded recipe: ${it.name}" } ?: "Recipe not found")
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recipe by ID: ${e.message}")
                _errorMessage.value = "Failed to get recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getUserSavedRecipe(userId: String): List<Recipe> {
        return try {
            val db = FirebaseFirestore.getInstance()

            // Bước 1: Lấy danh sách recipeId mà user đã bookmark
            val savedRecipesSnapshot = db.collection("savedRecipes")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val recipeIds = savedRecipesSnapshot.documents.mapNotNull { it.getString("recipeId") }

            if (recipeIds.isEmpty()) {
                Log.e(TAG, "Saved Recipe getted: " + recipeIds.count())
                return emptyList()
            } else {
                Log.e(TAG, "Saved Recipe getted: " + recipeIds.count())
            }

            // Bước 2: Lấy danh sách Recipe từ collection "recipes" dựa vào id
            val recipes = mutableListOf<Recipe>()

            // Firestore giới hạn 10 phần tử trong whereIn, nên cần chia nhỏ
            recipeIds.chunked(10).forEach { chunk ->
                val snapshot = db.collection("recipes")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()

                val chunkRecipes = snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
                recipes.addAll(chunkRecipes)
            }

            Log.e(TAG, "Saved Recipe getted final: " + recipes.count())
            recipes
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error fetching saved recipes for user $userId: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun searchRecipesByIngredient(ingredient: String): List<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Searching recipes with ingredient: $ingredient")
                if (ingredient.isBlank()) {
                    return@withContext emptyList()
                }

                // First try to get all recipes if not already loaded
                val allRecipes = if (_recipes.value.isEmpty()) {
                    recipeRepository.getAllRecipes()
                } else {
                    _recipes.value
                }

                // Filter recipes that contain the ingredient
                val matchingRecipes = allRecipes.filter { recipe ->
                    // Check if ingredient is in the name
                    recipe.name.contains(ingredient, ignoreCase = true) ||
                            // Check if ingredient is in the description
                            recipe.description.contains(ingredient, ignoreCase = true) ||
                            // Check if ingredient is in the ingredients list
                            recipe.ingredients.any { it.contains(ingredient, ignoreCase = true) }
                }

                // Sort by rating (highest first)
                val sortedRecipes = matchingRecipes.sortedByDescending { it.rating }

                // Limit to 5 recipes for recommendation
                val limitedRecipes = sortedRecipes.take(5)

                // Update the state
                _ingredientRecipes.value = limitedRecipes

                Log.d(TAG, "Found ${limitedRecipes.size} recipes with ingredient: $ingredient")
                return@withContext limitedRecipes
            } catch (e: Exception) {
                Log.e(TAG, "Error searching recipes by ingredient: ${e.message}")
                return@withContext emptyList()
            }
        }
    }

    /**
     * Save an image and create a recipe
     */
    fun saveImageAndCreateRecipe(
        context: Context,
        imageUri: Uri,
        name: String,
        description: String,
        ingredients: List<String>,
        instructions: List<String>,
        prepTime: Int,
        cookTime: Int,
        servings: Int,
        difficulty: String,
        categories: List<String>,
        authorId: String,
        authorName: String,
        calories: Int = 0,
        protein: Int = 0,
        carbs: Int = 0,
        fat: Int = 0,
        youtubeVideoId: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Saving image and creating recipe: $name")
                // Save image to local storage
                val imagePath = LocalImageStorage.saveImage(context, imageUri, LocalImageStorage.BLOG)

                // Create recipe with image path
                createRecipe(
                    name = name,
                    description = description,
                    ingredients = ingredients,
                    instructions = instructions,
                    prepTime = prepTime,
                    cookTime = cookTime,
                    servings = servings,
                    difficulty = difficulty,
                    categories = categories,
                    authorId = authorId,
                    authorName = authorName,
                    imageUrl = imagePath,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    youtubeVideoId = youtubeVideoId
                )

                Log.d(TAG, "Recipe created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving image and creating recipe: ${e.message}")
                _errorMessage.value = "Error saving image and creating recipe: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Enhance the createRecipe function to reload recipes after creation
    fun createRecipe(
        name: String,
        description: String,
        ingredients: List<String>,
        instructions: List<String>,
        prepTime: Int,
        cookTime: Int,
        servings: Int,
        difficulty: String,
        categories: List<String>,
        authorId: String,
        authorName: String,
        imageUrl: String = "",
        calories: Int = 0,
        protein: Int = 0,
        carbs: Int = 0,
        fat: Int = 0,
        youtubeVideoId: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Creating recipe: $name")
                val recipeId = UUID.randomUUID().toString()
                val creationDate = System.currentTimeMillis()

                val recipe = Recipe(
                    id = recipeId,
                    name = name,
                    description = description,
                    ingredients = ingredients,
                    instructions = instructions,
                    prepTime = prepTime,
                    cookTime = cookTime,
                    servings = servings,
                    difficulty = difficulty,
                    categories = categories,
                    authorId = authorId,
                    authorName = authorName,
                    imageUrl = imageUrl,
                    createdAt = creationDate,
                    rating = 0.0,
                    ratingCount = 0,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    youtubeVideoId = youtubeVideoId,
                    featured = false
                )

                val success = recipeRepository.createRecipe(recipe)

                if (success) {
                    Log.d(TAG, "Recipe created successfully")
                    _errorMessage.value = null
                    // Refresh recipes
                    loadAllRecipes()

                    // Log the creation for debugging
                    Log.d(TAG, "Recipe created with ID: $recipeId for author: $authorId")
                } else {
                    Log.e(TAG, "Failed to create recipe")
                    _errorMessage.value = "Failed to create recipe"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating recipe: ${e.message}")
                _errorMessage.value = "Error creating recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing recipe
     */
    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Updating recipe: ${recipe.name}")
                val success = recipeRepository.updateRecipe(recipe)

                if (success) {
                    Log.d(TAG, "Recipe updated successfully")
                    _currentRecipe.value = recipe
                    _errorMessage.value = null
                    // Refresh recipes
                    loadAllRecipes()
                } else {
                    Log.e(TAG, "Failed to update recipe")
                    _errorMessage.value = "Failed to update recipe"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating recipe: ${e.message}")
                _errorMessage.value = "Error updating recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a recipe
     */
    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Deleting recipe with ID: $recipeId")
                val success = recipeRepository.deleteRecipe(recipeId)

                if (success) {
                    Log.d(TAG, "Recipe deleted successfully")
                    // Remove the recipe from the lists
                    _recipes.value = _recipes.value.filter { it.id != recipeId }
                    _featuredRecipes.value = _featuredRecipes.value.filter { it.id != recipeId }
                    _popularRecipes.value = _popularRecipes.value.filter { it.id != recipeId }
                    _categoryRecipes.value = _categoryRecipes.value.filter { it.id != recipeId }

                    if (_currentRecipe.value?.id == recipeId) {
                        _currentRecipe.value = null
                    }

                    _errorMessage.value = null
                } else {
                    Log.e(TAG, "Failed to delete recipe")
                    _errorMessage.value = "Failed to delete recipe"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting recipe: ${e.message}")
                _errorMessage.value = "Error deleting recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a recipe with admin privileges (no user check)
     */
    fun adminDeleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Admin deleting recipe with ID: $recipeId")
                val success = recipeRepository.adminDeleteRecipe(recipeId)

                if (success) {
                    Log.d(TAG, "Recipe deleted successfully (admin)")
                    // Remove the recipe from the lists
                    _recipes.value = _recipes.value.filter { it.id != recipeId }
                    _featuredRecipes.value = _featuredRecipes.value.filter { it.id != recipeId }
                    _popularRecipes.value = _popularRecipes.value.filter { it.id != recipeId }
                    _categoryRecipes.value = _categoryRecipes.value.filter { it.id != recipeId }

                    if (_currentRecipe.value?.id == recipeId) {
                        _currentRecipe.value = null
                    }

                    _errorMessage.value = null
                } else {
                    Log.e(TAG, "Failed to delete recipe (admin)")
                    _errorMessage.value = "Failed to delete recipe (admin)"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting recipe (admin): ${e.message}")
                _errorMessage.value = "Error deleting recipe (admin): ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Bookmark a recipe for a user
     */
    fun bookmarkRecipe(recipeId: String, userId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Bookmarking recipe $recipeId for user $userId")

                // First check if the user document exists
                val userExists = userDocumentExists(userId)

                if (!userExists) {
                    // Create user document with savedRecipes array if it doesn't exist
                    createUserWithSavedRecipe(userId, recipeId)
                } else {
                    // User exists, update normally
                    val success = recipeRepository.bookmarkRecipe(recipeId, userId)

                    if (success) {
                        Log.d(TAG, "Recipe bookmarked successfully")
                        // Refresh saved recipes
                        loadSavedRecipes(userId)
                        _errorMessage.value = null
                    } else {
                        Log.e(TAG, "Failed to bookmark recipe")
                        _errorMessage.value = "Failed to bookmark recipe"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error bookmarking recipe: ${e.message}", e)
                _errorMessage.value = "Error bookmarking recipe: ${e.message}"
                throw e  // Re-throw to allow handling in UI
            }
        }
    }

    /**
     * Remove a bookmark for a recipe
     */
    fun unbookmarkRecipe(recipeId: String, userId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Unbookmarking recipe $recipeId for user $userId")
                val success = recipeRepository.unbookmarkRecipe(recipeId, userId)

                if (success) {
                    Log.d(TAG, "Recipe unbookmarked successfully")
                    // Refresh saved recipes
                    loadSavedRecipes(userId)
                    _errorMessage.value = null
                } else {
                    Log.e(TAG, "Failed to unbookmark recipe")
                    _errorMessage.value = "Failed to unbookmark recipe"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error unbookmarking recipe: ${e.message}")
                _errorMessage.value = "Error unbookmarking recipe: ${e.message}"
            }
        }
    }

    /**
     * Check if a recipe is saved by a user
     */
    suspend fun isRecipeSaved(recipeId: String, userId: String): Boolean {
        return try {
            Log.d(TAG, "Checking if recipe $recipeId is saved by user $userId")
            val isSaved = recipeRepository.isRecipeSaved(recipeId, userId)
            Log.d(TAG, "Recipe is saved: $isSaved")
            isSaved
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if recipe is saved: ${e.message}")
            _errorMessage.value = "Error checking if recipe is saved: ${e.message}"
            false
        }
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Check if user document exists in Firestore
     */
    private suspend fun userDocumentExists(userId: String): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val docSnapshot = db.collection("users").document(userId).get().await()
            docSnapshot.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user exists: ${e.message}", e)
            false
        }
    }

    /**
     * Create user document with initial saved recipe
     */
    private suspend fun createUserWithSavedRecipe(userId: String, recipeId: String): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val userData = hashMapOf(
                "id" to userId,
                "savedRecipes" to listOf(recipeId)
            )
            db.collection("users").document(userId).set(userData).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user document: ${e.message}", e)
            false
        }
    }
}
