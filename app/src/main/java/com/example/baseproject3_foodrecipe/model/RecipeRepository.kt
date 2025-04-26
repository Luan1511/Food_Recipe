package com.example.baseproject3_foodrecipe.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")
    private val usersCollection = db.collection("users")

    suspend fun getRecipes(): List<Recipe> {
        return try {
            withContext(Dispatchers.IO) {
                val snapshot = recipesCollection.get().await()
                snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Recipe::class.java)
                    } catch (e: Exception) {
                        Log.e("RecipeRepository", "Error converting document to Recipe: ${e.message}")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting recipes: ${e.message}")
            // Return empty list instead of null to avoid crashes
            emptyList()
        }
    }

    suspend fun getRecipesByCategory(category: String): List<Recipe> {
        return try {
            withContext(Dispatchers.IO) {
                val snapshot = recipesCollection
                    .whereArrayContains("categories", category)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Recipe::class.java)
                    } catch (e: Exception) {
                        Log.e("RecipeRepository", "Error converting document to Recipe: ${e.message}")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting category recipes: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRecipesByUser(userId: String): List<Recipe> {
        return try {
            withContext(Dispatchers.IO) {
                val snapshot = recipesCollection
                    .whereEqualTo("authorId", userId)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Recipe::class.java)
                    } catch (e: Exception) {
                        Log.e("RecipeRepository", "Error converting document to Recipe: ${e.message}")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting user recipes: ${e.message}")
            emptyList()
        }
    }

    suspend fun getSavedRecipes(userId: String): List<Recipe> {
        return try {
            withContext(Dispatchers.IO) {
                // Get user's saved recipe IDs
                val userDoc = usersCollection.document(userId).get().await()
                val user = userDoc.toObject(User::class.java)
                val savedRecipeIds = user?.savedRecipes ?: emptyList()

                if (savedRecipeIds.isEmpty()) {
                    return@withContext emptyList()
                }

                // Get recipes by IDs
                val recipes = mutableListOf<Recipe>()
                for (recipeId in savedRecipeIds) {
                    val recipeDoc = recipesCollection.document(recipeId).get().await()
                    recipeDoc.toObject(Recipe::class.java)?.let { recipes.add(it) }
                }

                recipes
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting saved recipes: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRecipe(recipeId: String): Recipe? {
        return try {
            withContext(Dispatchers.IO) {
                val document = recipesCollection.document(recipeId).get().await()
                document.toObject(Recipe::class.java)
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting recipe: ${e.message}")
            null
        }
    }

    suspend fun createRecipe(recipe: Recipe): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                recipesCollection.document(recipe.id).set(recipe).await()
                true
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error creating recipe: ${e.message}")
            false
        }
    }

    suspend fun updateRecipe(recipe: Recipe): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                recipesCollection.document(recipe.id).set(recipe).await()
                true
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error updating recipe: ${e.message}")
            false
        }
    }

    suspend fun deleteRecipe(recipeId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                recipesCollection.document(recipeId).delete().await()
                true
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error deleting recipe: ${e.message}")
            false
        }
    }

    suspend fun bookmarkRecipe(recipeId: String, userId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Get current user data
                val userDoc = usersCollection.document(userId).get().await()
                val savedRecipes = userDoc.get("savedRecipes") as? List<String> ?: emptyList()

                // Add recipe ID if not already saved
                if (recipeId !in savedRecipes) {
                    val updatedSavedRecipes = savedRecipes + recipeId
                    usersCollection.document(userId).update("savedRecipes", updatedSavedRecipes).await()
                }

                true
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error bookmarking recipe: ${e.message}")
            false
        }
    }

    suspend fun unbookmarkRecipe(recipeId: String, userId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Get current user data
                val userDoc = usersCollection.document(userId).get().await()
                val savedRecipes = userDoc.get("savedRecipes") as? List<String> ?: emptyList()

                // Remove recipe ID if saved
                if (recipeId in savedRecipes) {
                    val updatedSavedRecipes = savedRecipes.filter { it != recipeId }
                    usersCollection.document(userId).update("savedRecipes", updatedSavedRecipes).await()
                }

                true
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error unbookmarking recipe: ${e.message}")
            false
        }
    }
}
