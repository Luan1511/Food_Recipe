package com.example.baseproject3_foodrecipe.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository for managing recipes
 */
class RecipeRepository {
    private val TAG = "RecipeRepository"
    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")
    private val usersCollection = db.collection("users")

    /**
     * Get all recipes
     */
    suspend fun getAllRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching all recipes")
            val snapshot = recipesCollection
                .get()
                .await()

            val recipes = mutableListOf<Recipe>()

            for (document in snapshot.documents) {
                try {
                    val data = document.data
                    Log.d(TAG, "Document ID: ${document.id}, data: $data")

                    val recipe = document.toObject(Recipe::class.java)
                    if (recipe != null) {
                        recipes.add(recipe)
                    } else {
                        Log.e(TAG, "Failed to convert document to Recipe: null result")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Recipe: ${e.message}")
                }
            }

            Log.d(TAG, "Fetched ${recipes.size} recipes")
            return@withContext recipes
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all recipes: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    /**
     * Get featured recipes
     */
    suspend fun getFeaturedRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching featured recipes")
            // Use the field name that matches your Firestore documents
            val snapshot = recipesCollection
                .whereEqualTo("isFeatured", true)
                .limit(10)
                .get()
                .await()

            val recipes = mutableListOf<Recipe>()

            for (document in snapshot.documents) {
                try {
                    val data = document.data
                    Log.d(TAG, "Featured document ID: ${document.id}, data: $data")

                    val recipe = document.toObject(Recipe::class.java)
                    if (recipe != null) {
                        recipes.add(recipe)
                    } else {
                        Log.e(TAG, "Failed to convert featured document to Recipe: null result")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting featured document to Recipe: ${e.message}")
                }
            }

            Log.d(TAG, "Fetched ${recipes.size} featured recipes")
            return@withContext recipes
        } catch (e: Exception) {
            Log.e(TAG, "Error getting featured recipes: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    /**
     * Get popular recipes
     */
    suspend fun getPopularRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching popular recipes")
            val snapshot = recipesCollection
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            val recipes = mutableListOf<Recipe>()

            for (document in snapshot.documents) {
                try {
                    val data = document.data
                    Log.d(TAG, "Popular document ID: ${document.id}, data: $data")

                    val recipe = document.toObject(Recipe::class.java)
                    if (recipe != null) {
                        recipes.add(recipe)
                    } else {
                        Log.e(TAG, "Failed to convert popular document to Recipe: null result")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting popular document to Recipe: ${e.message}")
                }
            }

            Log.d(TAG, "Fetched ${recipes.size} popular recipes")
            return@withContext recipes
        } catch (e: Exception) {
            Log.e(TAG, "Error getting popular recipes: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    /**
     * Get saved recipes for a user
     */
    suspend fun getSavedRecipes(userId: String): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching saved recipes for user: $userId")
            val userDoc = usersCollection.document(userId).get().await()
            val savedRecipeIds = userDoc.get("savedRecipes") as? List<String> ?: emptyList()

            Log.d(TAG, "User has ${savedRecipeIds.size} saved recipes")

            if (savedRecipeIds.isEmpty()) {
                return@withContext emptyList()
            }

            val recipes = mutableListOf<Recipe>()

            // Firestore has a limit of 10 items in a whereIn query, so we need to batch
            savedRecipeIds.chunked(10).forEach { chunk ->
                val snapshot = recipesCollection
                    .whereIn("id", chunk)
                    .get()
                    .await()

                for (document in snapshot.documents) {
                    try {
                        val recipe = document.toObject(Recipe::class.java)
                        if (recipe != null) {
                            recipes.add(recipe)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to Recipe: ${e.message}")
                    }
                }
            }

            Log.d(TAG, "Fetched ${recipes.size} saved recipes")
            return@withContext recipes
        } catch (e: Exception) {
            Log.e(TAG, "Error getting saved recipes: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    /**
     * Get a recipe by ID
     */
    suspend fun getRecipeById(recipeId: String): Recipe? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching recipe with ID: $recipeId")
            // Try direct document access first
            val documentSnapshot = recipesCollection.document(recipeId).get().await()

            if (documentSnapshot.exists()) {
                val recipe = documentSnapshot.toObject(Recipe::class.java)
                Log.d(TAG, "Found recipe by direct ID: ${recipe?.name}")
                return@withContext recipe
            }

            // If direct access fails, try query
            val snapshot = recipesCollection
                .whereEqualTo("id", recipeId)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isEmpty()) {
                Log.d(TAG, "No recipe found with ID: $recipeId")
                return@withContext null
            }

            val recipe = snapshot.documents[0].toObject(Recipe::class.java)
            Log.d(TAG, "Found recipe by query: ${recipe?.name}")
            return@withContext recipe
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recipe by ID: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Get recipes by author ID
     */
    suspend fun getRecipesByAuthor(authorId: String): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching recipes by author ID: $authorId")

            // Query recipes using the authorId field
            val snapshot = recipesCollection
                .whereEqualTo("authorId", authorId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d(TAG, "Query returned ${snapshot.documents.size} documents for author $authorId")

            // Log each document for debugging
            snapshot.documents.forEach { doc ->
                Log.d(TAG, "Document ID: ${doc.id}, authorId: ${doc.getString("authorId")}")
            }

            val recipes = snapshot.documents.mapNotNull { document ->
                try {
                    val recipe = document.toObject(Recipe::class.java)
                    Log.d(TAG, "Converted document to recipe: ${recipe?.name}")
                    recipe
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Recipe: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Fetched ${recipes.size} recipes by author")
            return@withContext recipes
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recipes by author: ${e.message}")
            return@withContext emptyList()
        }
    }

    /**
     * Create a new recipe
     */
    suspend fun createRecipe(recipe: Recipe): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creating new recipe: ${recipe.name}")
            recipesCollection.document(recipe.id).set(recipe).await()
            Log.d(TAG, "Recipe created successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating recipe: ${e.message}")
            false
        }
    }

    /**
     * Update an existing recipe
     */
    suspend fun updateRecipe(recipe: Recipe): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating recipe: ${recipe.name}")
            recipesCollection.document(recipe.id).set(recipe).await()
            Log.d(TAG, "Recipe updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating recipe: ${e.message}")
            false
        }
    }

    /**
     * Delete a recipe
     */
    suspend fun deleteRecipe(recipeId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting recipe with ID: $recipeId")
            recipesCollection.document(recipeId).delete().await()
            Log.d(TAG, "Recipe deleted successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting recipe: ${e.message}")
            false
        }
    }

    /**
     * Delete a recipe with admin privileges (no user check)
     */
    suspend fun adminDeleteRecipe(recipeId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext deleteRecipe(recipeId)
    }

    /**
     * Search recipes by query
     */
    suspend fun searchRecipes(query: String): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Searching recipes with query: $query")
            if (query.isBlank()) {
                return@withContext emptyList()
            }

            // This is a simple client-side search implementation
            // For a real app, you would use Firestore's search capabilities or a dedicated search service
            val snapshot = recipesCollection.get().await()

            val recipes = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Recipe::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Recipe: ${e.message}")
                    null
                }
            }.filter { recipe ->
                recipe.name.contains(query, ignoreCase = true) ||
                        recipe.description.contains(query, ignoreCase = true) ||
                        recipe.categories.any { it.contains(query, ignoreCase = true) } ||
                        recipe.ingredients.any { it.contains(query, ignoreCase = true) }
            }

            Log.d(TAG, "Found ${recipes.size} recipes matching query")
            return@withContext recipes
        } catch (e: Exception) {
            Log.e(TAG, "Error searching recipes: ${e.message}")
            return@withContext emptyList()
        }
    }

    /**
     * Get recipes by category
     */
    suspend fun getRecipesByCategory(category: String): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching recipes by category: $category")
            val snapshot = recipesCollection
                .whereArrayContains("categories", category)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val recipes = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Recipe::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Recipe: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Fetched ${recipes.size} recipes by category")
            return@withContext recipes
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recipes by category: ${e.message}")
            return@withContext emptyList()
        }
    }

    /**
     * Get recipes by cuisine
     */
    suspend fun getRecipesByCuisine(cuisine: String): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching recipes by cuisine: $cuisine")
            val snapshot = recipesCollection
                .whereEqualTo("cuisine", cuisine)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val recipes = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Recipe::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Recipe: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Fetched ${recipes.size} recipes by cuisine")
            return@withContext recipes
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recipes by cuisine: ${e.message}")
            return@withContext emptyList()
        }
    }

    /**
     * Bookmark a recipe for a user
     */
    suspend fun bookmarkRecipe(recipeId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Bookmarking recipe $recipeId for user $userId")

            // Check if user document exists first
            val userDoc = usersCollection.document(userId).get().await()

            if (!userDoc.exists()) {
                // Create user document with savedRecipes array
                val userData = mapOf(
                    "id" to userId,
                    "savedRecipes" to listOf(recipeId)
                )
                usersCollection.document(userId).set(userData).await()
            } else {
                // Update existing user document
                usersCollection.document(userId).update(
                    "savedRecipes", com.google.firebase.firestore.FieldValue.arrayUnion(recipeId)
                ).await()
            }

            Log.d(TAG, "Recipe bookmarked successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error bookmarking recipe: ${e.message}", e)
            false
        }
    }

    /**
     * Remove a bookmark for a recipe
     */
    suspend fun unbookmarkRecipe(recipeId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Unbookmarking recipe $recipeId for user $userId")
            usersCollection.document(userId).update(
                "savedRecipes", com.google.firebase.firestore.FieldValue.arrayRemove(recipeId)
            ).await()
            Log.d(TAG, "Recipe unbookmarked successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unbookmarking recipe: ${e.message}")
            false
        }
    }

    /**
     * Check if a recipe is saved by a user
     */
    suspend fun isRecipeSaved(recipeId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Checking if recipe $recipeId is saved by user $userId")
            val userDoc = usersCollection.document(userId).get().await()
            val savedRecipes = userDoc.get("savedRecipes") as? List<String> ?: emptyList()
            val isSaved = savedRecipes.contains(recipeId)
            Log.d(TAG, "Recipe is saved: $isSaved")
            return@withContext isSaved
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if recipe is saved: ${e.message}")
            return@withContext false
        }
    }
}
