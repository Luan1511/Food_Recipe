package com.example.baseproject3_foodrecipe.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val followsCollection = db.collection("follows")

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

    suspend fun isFollowingUser(currentUserId: String, targetUserId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val followDoc = followsCollection
                    .whereEqualTo("followerId", currentUserId)
                    .whereEqualTo("followingId", targetUserId)
                    .get()
                    .await()

                !followDoc.isEmpty
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking follow status: ${e.message}")
            false
        }
    }

    suspend fun followUser(currentUserId: String, targetUserId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Check if already following
                if (isFollowingUser(currentUserId, targetUserId)) {
                    return@withContext true
                }

                // Create follow relationship
                val followId = "${currentUserId}_${targetUserId}"
                val followData = hashMapOf(
                    "id" to followId,
                    "followerId" to currentUserId,
                    "followingId" to targetUserId,
                    "timestamp" to System.currentTimeMillis()
                )

                followsCollection.document(followId).set(followData).await()

                // Update current user's following count
                val currentUser = getUser(currentUserId)
                if (currentUser != null) {
                    usersCollection.document(currentUserId)
                        .update("following", currentUser.following + 1)
                        .await()
                }

                // Update target user's followers count
                val targetUser = getUser(targetUserId)
                if (targetUser != null) {
                    usersCollection.document(targetUserId)
                        .update("followers", targetUser.followers + 1)
                        .await()
                }

                true
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error following user: ${e.message}")
            false
        }
    }

    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Check if actually following
                if (!isFollowingUser(currentUserId, targetUserId)) {
                    return@withContext true
                }

                // Delete follow relationship
                val followId = "${currentUserId}_${targetUserId}"
                followsCollection.document(followId).delete().await()

                // Update current user's following count
                val currentUser = getUser(currentUserId)
                if (currentUser != null && currentUser.following > 0) {
                    usersCollection.document(currentUserId)
                        .update("following", currentUser.following - 1)
                        .await()
                }

                // Update target user's followers count
                val targetUser = getUser(targetUserId)
                if (targetUser != null && targetUser.followers > 0) {
                    usersCollection.document(targetUserId)
                        .update("followers", targetUser.followers - 1)
                        .await()
                }

                true
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error unfollowing user: ${e.message}")
            false
        }
    }

    suspend fun searchUsers(query: String): List<User> {
        return try {
            withContext(Dispatchers.IO) {
                // Get all users and filter locally
                // In a real app, you'd use Firestore's search capabilities
                val snapshot = usersCollection.get().await()
                val allUsers = snapshot.documents.mapNotNull { it.toObject(User::class.java) }

                allUsers.filter { user ->
                    user.name.contains(query, ignoreCase = true) ||
                            user.username.contains(query, ignoreCase = true) ||
                            user.email.contains(query, ignoreCase = true) ||
                            user.bio.contains(query, ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error searching users: ${e.message}")
            emptyList()
        }
    }
}
