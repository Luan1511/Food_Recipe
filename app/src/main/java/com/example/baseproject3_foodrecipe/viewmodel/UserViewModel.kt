package com.example.baseproject3_foodrecipe.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.model.User
import com.example.baseproject3_foodrecipe.model.UserRepository
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val userRecipes: StateFlow<List<Recipe>> = _userRecipes.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

    // Add getCurrentUser method to fetch the current logged-in user
    fun getCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getCurrentUser()
                _currentUser.value = user
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get current user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getUserById(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getUser(userId)
                _currentUser.value = user
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val users = userRepository.getAllUsers()
                _allUsers.value = users
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val users = userRepository.searchUsers(query)
                _searchResults.value = users
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                val success = userRepository.createUser(user)

                if (success) {
                    _currentUser.value = user
                    _operationSuccess.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to create user"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error creating user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserProfile(
        userId: String,
        name: String,
        bio: String,
        context: Context? = null,
        profileImageUri: Uri? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                // Get current user
                val currentUser = userRepository.getUser(userId)

                if (currentUser != null) {
                    // Process profile image if provided
                    var profileImageUrl = currentUser.profileImageUrl
                    if (context != null && profileImageUri != null) {
                        profileImageUrl = LocalImageStorage.saveImage(context, profileImageUri, LocalImageStorage.PROFILE)
                    }

                    // Create updated user
                    val updatedUser = currentUser.copy(
                        name = name,
                        bio = bio,
                        profileImageUrl = profileImageUrl
                    )

                    // Update user
                    val success = userRepository.updateUser(updatedUser)

                    if (success) {
                        _currentUser.value = updatedUser
                        _operationSuccess.value = true
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = "Failed to update user profile"
                    }
                } else {
                    _errorMessage.value = "User not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating user profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                val success = userRepository.updateUser(user)

                if (success) {
                    _currentUser.value = user
                    _operationSuccess.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to update user"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun followUser(currentUserId: String, targetUserId: String): Boolean {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                val success = userRepository.followUser(currentUserId, targetUserId)
                if (success) {
                    _operationSuccess.value = true
                    // Refresh user data
                    getUserById(targetUserId)
                } else {
                    _errorMessage.value = "Failed to follow user"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to follow user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
        return true // Return true to avoid type mismatch
    }

    fun unfollowUser(currentUserId: String, targetUserId: String): Boolean {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                val success = userRepository.unfollowUser(currentUserId, targetUserId)
                if (success) {
                    _operationSuccess.value = true
                    // Refresh user data
                    getUserById(targetUserId)
                } else {
                    _errorMessage.value = "Failed to unfollow user"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to unfollow user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
        return true // Return true to avoid type mismatch
    }

    suspend fun isFollowingUser(currentUserId: String, targetUserId: String): Boolean {
        return try {
            userRepository.isFollowingUser(currentUserId, targetUserId)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to check following status: ${e.message}"
            false
        }
    }

    fun setAdminStatus(userId: String, isAdmin: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                val success = userRepository.setAdminStatus(userId, isAdmin)
                if (success) {
                    _operationSuccess.value = true
                    // Refresh user data
                    getUserById(userId)
                    loadAllUsers()
                } else {
                    _errorMessage.value = "Failed to set admin status"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to set admin status: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setChefStatus(userId: String, isChef: Boolean, chefTitle: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                val success = userRepository.setChefStatus(userId, isChef, chefTitle)
                if (success) {
                    _operationSuccess.value = true
                    // Refresh user data
                    getUserById(userId)
                    loadAllUsers()
                } else {
                    _errorMessage.value = "Failed to set chef status"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to set chef status: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load recipes created by a specific user
     */
    fun loadUserRecipes(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipes = userRepository.getRecipesByAuthor(userId)
                _userRecipes.value = recipes
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error loading user recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetOperationSuccess() {
        _operationSuccess.value = false
    }
}
