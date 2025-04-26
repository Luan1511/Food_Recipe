package com.example.baseproject3_foodrecipe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.model.RecipeRepository
import com.example.baseproject3_foodrecipe.model.User
import com.example.baseproject3_foodrecipe.model.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val recipeRepository = RecipeRepository()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val userRecipes: StateFlow<List<Recipe>> = _userRecipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // For demo purposes, we'll set a default user
    // In a real app, this would come from authentication
    private var defaultUserId = "user123"

    init {
        // Create a default user if not exists and load it
        viewModelScope.launch {
            createDefaultUserIfNotExists()
            getUserById(defaultUserId)
        }
    }

    private suspend fun createDefaultUserIfNotExists() {
        val existingUser = userRepository.getUser(defaultUserId)
        if (existingUser == null) {
            val defaultUser = User(
                id = defaultUserId,
                name = "Chef Michael",
                username = "chefmichael",
                email = "chef@example.com",
                bio = "Professional chef specializing in Italian cuisine",
//                isChef = true,
                chefTitle = "Executive Chef"
            )
            userRepository.createUser(defaultUser)
        }
    }

    fun getUserById(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getUser(userId)
                _currentUser.value = user

                // Also load user's recipes
                if (user != null) {
                    loadUserRecipes(userId)
                }

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserRecipes(userId: String) {
        viewModelScope.launch {
            try {
                val recipes = recipeRepository.getRecipesByUser(userId)
                _userRecipes.value = recipes
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load user recipes: ${e.message}"
            }
        }
    }

    fun followUser(targetUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUser.value?.id ?: return@launch
//                userRepository.followUser(currentUserId, targetUserId)

                // Refresh current user to update UI
                getUserById(currentUserId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to follow user: ${e.message}"
            }
        }
    }

    fun unfollowUser(targetUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUser.value?.id ?: return@launch
//                userRepository.unfollowUser(currentUserId, targetUserId)

                // Refresh current user to update UI
                getUserById(currentUserId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to unfollow user: ${e.message}"
            }
        }
    }

    fun updateUserProfile(
        name: String,
        bio: String,
        profileImageUrl: String
    ) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUser.value ?: return@launch
                val updatedUser = currentUser.copy(
                    name = name,
                    bio = bio,
                    profileImageUrl = profileImageUrl
                )

                userRepository.updateUser(updatedUser)
                _currentUser.value = updatedUser
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile: ${e.message}"
            }
        }
    }

    // For demo purposes, get the current user ID
    fun getCurrentUserId(): String {
        return defaultUserId
    }
}
