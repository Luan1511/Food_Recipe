package com.example.baseproject3_foodrecipe.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getUser(userId: String): User? {
        return try {
            withContext(Dispatchers.IO) {
                val document = usersCollection.document(userId).get().await()
                document.toObject(User::class.java)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user: ${e.message}")
            null
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            withContext(Dispatchers.IO) {
                val snapshot = usersCollection
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .await()

                if (snapshot.documents.isNotEmpty()) {
                    snapshot.documents[0].toObject(User::class.java)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user by email: ${e.message}")
            null
        }
    }

    suspend fun createUser(user: User): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                usersCollection.document(user.id).set(user).await()
                true
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error creating user: ${e.message}")
            false
        }
    }

    suspend fun updateUser(user: User): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                usersCollection.document(user.id).set(user).await()
                true
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user: ${e.message}")
            false
        }
    }

    suspend fun deleteUser(userId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                usersCollection.document(userId).delete().await()
                true
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error deleting user: ${e.message}")
            false
        }
    }

    suspend fun addRecipeToUser(userId: String, recipeId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val user = getUser(userId)
                if (user != null) {
                    val updatedRecipes = user.recipes.toMutableList()
                    if (!updatedRecipes.contains(recipeId)) {
                        updatedRecipes.add(recipeId)
                        usersCollection.document(userId)
                            .update("recipes", updatedRecipes)
                            .await()
                        true
                    } else {
                        // Recipe already in user's list
                        true
                    }
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding recipe to user: ${e.message}")
            false
        }
    }

    suspend fun saveRecipe(userId: String, recipeId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val user = getUser(userId)
                if (user != null) {
                    val savedRecipes = user.savedRecipes.toMutableList()
                    if (!savedRecipes.contains(recipeId)) {
                        savedRecipes.add(recipeId)
                        usersCollection.document(userId)
                            .update("savedRecipes", savedRecipes)
                            .await()
                        true
                    } else {
                        // Recipe already saved
                        true
                    }
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving recipe: ${e.message}")
            false
        }
    }

    suspend fun unsaveRecipe(userId: String, recipeId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val user = getUser(userId)
                if (user != null) {
                    val savedRecipes = user.savedRecipes.toMutableList()
                    if (savedRecipes.contains(recipeId)) {
                        savedRecipes.remove(recipeId)
                        usersCollection.document(userId)
                            .update("savedRecipes", savedRecipes)
                            .await()
                        true
                    } else {
                        // Recipe not in saved list
                        true
                    }
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error unsaving recipe: ${e.message}")
            false
        }
    }
}
