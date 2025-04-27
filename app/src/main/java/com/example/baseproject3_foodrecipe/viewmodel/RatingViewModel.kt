package com.example.baseproject3_foodrecipe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.Rating
import com.example.baseproject3_foodrecipe.model.RatingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RatingViewModel : ViewModel() {
    private val repository = RatingRepository()

    private val _userRating = MutableStateFlow<Double?>(null)
    val userRating: StateFlow<Double?> = _userRating.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun getUserRating(recipeId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val rating = repository.getUserRating(recipeId, userId)
                _userRating.value = rating?.value
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load rating: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rateRecipe(recipeId: String, userId: String, value: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val rating = Rating(
                    recipeId = recipeId,
                    userId = userId,
                    value = value
                )

                val success = repository.addOrUpdateRating(rating)

                if (success) {
                    _userRating.value = value
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to rate recipe"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to rate recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
