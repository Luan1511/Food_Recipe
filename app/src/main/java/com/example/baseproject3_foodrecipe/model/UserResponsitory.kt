package com.example.baseproject3_foodrecipe.model

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository for managing users
 */
class UserRepository {
    private val auth = FirebaseAuth.getInstance()

    // In-memory storage for users (replace with database in production)
    private val users = mutableListOf<User>()

    // In-memory storage for user-recipe relationships
    private val userRecipes = mutableMapOf<String, MutableList<Recipe>>() // userId -> List<Recipe>

    init {
        // Add some sample users for testing
        if (users.isEmpty()) {
            users.add(
                User(
                    id = "sample_user_1",
                    name = "John Doe",
                    email = "john@example.com",
                    bio = "Food enthusiast and home chef",
                    profileImageUrl = "",
                    followers = listOf("sample_user_2"),
                    following = listOf("sample_user_3"),
                    isAdmin = false,
                    isChef = true,
                    chefTitle = "Home Chef"
                )
            )
            users.add(
                User(
                    id = "sample_user_2",
                    name = "Jane Smith",
                    email = "jane@example.com",
                    bio = "Professional pastry chef",
                    profileImageUrl = "",
                    followers = listOf("sample_user_1"),
                    following = listOf(),
                    isAdmin = false,
                    isChef = true,
                    chefTitle = "Pastry Chef"
                )
            )
            users.add(
                User(
                    id = "sample_user_3",
                    name = "Admin User",
                    email = "admin@example.com",
                    bio = "Site administrator",
                    profileImageUrl = "",
                    followers = listOf(),
                    following = listOf(),
                    isAdmin = true,
                    isChef = false,
                    chefTitle = ""
                )
            )
        }
    }

    /**
     * Get the current logged-in user
     */
    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null

        // First check if user exists in our local cache
        var user = users.find { it.id == firebaseUser.uid }

        // If not found, create a new user object from Firebase user
        if (user == null) {
            user = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "User",
                email = firebaseUser.email ?: "",
                profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                bio = "",
                followers = emptyList(),
                following = emptyList(),
                isAdmin = false,
                isChef = false
            )

            // Add to our local cache
            users.add(user)
        }

        return user
    }

    /**
     * Get a user by ID
     */
    suspend fun getUser(userId: String): User? = withContext(Dispatchers.IO) {
        // First check if this is the current Firebase user
        val firebaseUser = auth.currentUser
        if (firebaseUser != null && firebaseUser.uid == userId) {
            // Return current user or create if not in cache
            return@withContext getCurrentUser()
        }

        // Otherwise check our local cache
        var user = users.find { it.id == userId }

        // If not found and this is a valid Firebase user ID, try to get from Firebase
        if (user == null) {
            try {
                // Create a default user with this ID if we can't find it
                // In a real app, you would fetch from Firestore/database
                user = User(
                    id = userId,
                    name = "User $userId",
                    email = "user$userId@example.com",
                    bio = "No bio available",
                    profileImageUrl = "",
                    followers = emptyList(),
                    following = emptyList()
                )

                // Add to our local cache
                users.add(user)
            } catch (e: Exception) {
                println("Error getting user: ${e.message}")
                return@withContext null
            }
        }

        return@withContext user
    }

    /**
     * Get all users
     */
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        return@withContext users.toList()
    }

    /**
     * Search users by name or email
     */
    suspend fun searchUsers(query: String): List<User> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext emptyList()
        }

        return@withContext users.filter { user ->
            user.name.contains(query, ignoreCase = true) ||
                    user.email.contains(query, ignoreCase = true)
        }
    }

    /**
     * Create a new user
     */
    suspend fun createUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if user with same email already exists
            if (users.any { it.email == user.email }) {
                return@withContext false
            }

            users.add(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update an existing user
     */
    suspend fun updateUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            val index = users.indexOfFirst { it.id == user.id }
            if (index >= 0) {
                users[index] = user
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Follow a user
     */
    suspend fun followUser(currentUserId: String, targetUserId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentUserIndex = users.indexOfFirst { it.id == currentUserId }
            val targetUserIndex = users.indexOfFirst { it.id == targetUserId }

            if (currentUserIndex >= 0 && targetUserIndex >= 0) {
                // Update current user's following list
                val currentUser = users[currentUserIndex]
                val updatedFollowing = ArrayList<String>(currentUser.following)
                if (!updatedFollowing.contains(targetUserId)) {
                    updatedFollowing.add(targetUserId)
                }
                users[currentUserIndex] = currentUser.copy(following = updatedFollowing)

                // Update target user's followers list
                val targetUser = users[targetUserIndex]
                val updatedFollowers = ArrayList<String>(targetUser.followers)
                if (!updatedFollowers.contains(currentUserId)) {
                    updatedFollowers.add(currentUserId)
                }
                users[targetUserIndex] = targetUser.copy(followers = updatedFollowers)

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Unfollow a user
     */
    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentUserIndex = users.indexOfFirst { it.id == currentUserId }
            val targetUserIndex = users.indexOfFirst { it.id == targetUserId }

            if (currentUserIndex >= 0 && targetUserIndex >= 0) {
                // Update current user's following list
                val currentUser = users[currentUserIndex]
                val updatedFollowing = ArrayList<String>(currentUser.following)
                updatedFollowing.remove(targetUserId)
                users[currentUserIndex] = currentUser.copy(following = updatedFollowing)

                // Update target user's followers list
                val targetUser = users[targetUserIndex]
                val updatedFollowers = ArrayList<String>(targetUser.followers)
                updatedFollowers.remove(currentUserId)
                users[targetUserIndex] = targetUser.copy(followers = updatedFollowers)

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if a user is following another user
     */
    suspend fun isFollowingUser(currentUserId: String, targetUserId: String): Boolean = withContext(Dispatchers.IO) {
        val currentUser = users.find { it.id == currentUserId }
        return@withContext currentUser?.following?.contains(targetUserId) ?: false
    }

    /**
     * Set admin status for a user
     */
    suspend fun setAdminStatus(userId: String, isAdmin: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val index = users.indexOfFirst { it.id == userId }
            if (index >= 0) {
                val user = users[index]
                users[index] = user.copy(isAdmin = isAdmin)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Set chef status for a user
     */
    suspend fun setChefStatus(userId: String, isChef: Boolean, chefTitle: String = ""): Boolean = withContext(Dispatchers.IO) {
        try {
            val index = users.indexOfFirst { it.id == userId }
            if (index >= 0) {
                val user = users[index]
                users[index] = user.copy(
                    isChef = isChef,
                    chefTitle = if (isChef) chefTitle else ""
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get recipes created by a specific user
     */
    suspend fun getRecipesByAuthor(authorId: String): List<Recipe> = withContext(Dispatchers.IO) {
        return@withContext userRecipes[authorId]?.toList() ?: emptyList()
    }

    /**
     * Add a recipe to a user's recipes
     */
    suspend fun addRecipeToUser(userId: String, recipe: Recipe): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!userRecipes.containsKey(userId)) {
                userRecipes[userId] = mutableListOf()
            }

            userRecipes[userId]?.add(recipe)
            true
        } catch (e: Exception) {
            false
        }
    }
}
