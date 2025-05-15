package com.example.baseproject3_foodrecipe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.Rating
import com.example.baseproject3_foodrecipe.model.RatingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class RatingViewModel : ViewModel() {
    private val ratingRepository = RatingRepository()

    private val _userRating = MutableStateFlow<Rating?>(null)
    val userRating: StateFlow<Rating?> = _userRating.asStateFlow()

    private val _averageRating = MutableStateFlow(0.0f)
    val averageRating: StateFlow<Float> = _averageRating.asStateFlow()

    private val _ratingCount = MutableStateFlow(0)
    val ratingCount: StateFlow<Int> = _ratingCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun getUserRatingForRecipe(userId: String, recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val rating = ratingRepository.getUserRatingForRecipe(userId, recipeId)
                _userRating.value = rating
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get user rating: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getUserRatingForBlog(userId: String, blogId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val rating = ratingRepository.getUserRatingForBlog(userId, blogId)
                _userRating.value = rating
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get user rating: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAverageRatingForRecipe(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = ratingRepository.getAverageRatingForRecipe(recipeId)
                _averageRating.value = result.first
                _ratingCount.value = result.second
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get average rating: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAverageRatingForBlog(blogId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = ratingRepository.getAverageRatingForBlog(blogId)
                _averageRating.value = result.first
                _ratingCount.value = result.second
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get average rating: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addRating(
        recipeId: String? = null,
        blogId: String? = null,
        userId: String,
        userName: String,
        value: Float,
        comment: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ratingId = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()

                val rating = Rating(
                    id = ratingId,
                    recipeId = recipeId ?: "",
                    blogId = blogId ?: "",
                    userId = userId,
                    userName = userName,
                    value = value.toDouble(), // Convert Float to Double
                    comment = comment,
                    timestamp = timestamp
                )

                val success = ratingRepository.addRating(rating)

                if (success) {
                    _userRating.value = rating

                    // Update average rating
                    if (recipeId != null && recipeId.isNotEmpty()) {
                        getAverageRatingForRecipe(recipeId)
                    }
                    if (blogId != null && blogId.isNotEmpty()) {
                        getAverageRatingForBlog(blogId)
                    }

                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to add rating"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error adding rating: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRating(
        ratingId: String,
        userId: String,
        value: Float,
        comment: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentRating = ratingRepository.getRatingById(ratingId)

                if (currentRating == null) {
                    _errorMessage.value = "Rating not found"
                    _isLoading.value = false
                    return@launch
                }

                if (currentRating.userId != userId) {
                    _errorMessage.value = "You can only update your own ratings"
                    _isLoading.value = false
                    return@launch
                }

                val updatedRating = currentRating.copy(
                    value = value.toDouble(), // Convert Float to Double
                    comment = comment,
                    timestamp = System.currentTimeMillis()
                )

                val success = ratingRepository.updateRating(updatedRating)

                if (success) {
                    _userRating.value = updatedRating

                    // Update average rating
                    updatedRating.recipeId?.let {
                        if (it.isNotEmpty()) getAverageRatingForRecipe(it)
                    }
                    updatedRating.blogId?.let {
                        if (it.isNotEmpty()) getAverageRatingForBlog(it)
                    }

                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to update rating"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating rating: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRating(ratingId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val rating = ratingRepository.getRatingById(ratingId)

                if (rating == null) {
                    _errorMessage.value = "Rating not found"
                    _isLoading.value = false
                    return@launch
                }

                val success = ratingRepository.deleteRating(ratingId)

                if (success) {
                    _userRating.value = null

                    // Update average rating
                    rating.recipeId?.let {
                        if (it.isNotEmpty()) getAverageRatingForRecipe(it)
                    }
                    rating.blogId?.let {
                        if (it.isNotEmpty()) getAverageRatingForBlog(it)
                    }

                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to delete rating"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting rating: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
