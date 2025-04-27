package com.example.baseproject3_foodrecipe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.Comment
import com.example.baseproject3_foodrecipe.model.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentViewModel : ViewModel() {
    private val repository = CommentRepository()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun getCommentsByRecipe(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipeComments = repository.getCommentsByRecipe(recipeId)
                _comments.value = recipeComments
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load comments: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addComment(recipeId: String, userId: String, userName: String, userImageUrl: String, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val comment = Comment(
                    recipeId = recipeId,
                    userId = userId,
                    userName = userName,
                    userImageUrl = userImageUrl,
                    content = content
                )

                val success = repository.addComment(comment)

                if (success) {
                    // Refresh comments
                    getCommentsByRecipe(recipeId)
                } else {
                    _errorMessage.value = "Failed to add comment"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add comment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteComment(commentId: String, recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.deleteComment(commentId)

                if (success) {
                    // Refresh comments
                    getCommentsByRecipe(recipeId)
                } else {
                    _errorMessage.value = "Failed to delete comment"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete comment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likeComment(commentId: String, recipeId: String) {
        viewModelScope.launch {
            try {
                val success = repository.likeComment(commentId)

                if (success) {
                    // Refresh comments
                    getCommentsByRecipe(recipeId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to like comment: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
