package com.example.baseproject3_foodrecipe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.Comment
import com.example.baseproject3_foodrecipe.model.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CommentViewModel : ViewModel() {
    private val commentRepository = CommentRepository()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadCommentsByRecipeId(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val commentsList = commentRepository.getCommentsByRecipeId(recipeId)
                _comments.value = commentsList
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load comments: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCommentsByBlogId(blogId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val commentsList = commentRepository.getCommentsByBlogId(blogId)
                _comments.value = commentsList
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load comments: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addComment(
        recipeId: String? = null,
        blogId: String? = null,
        userId: String,
        userName: String,
        userProfileImage: String,
        content: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val commentId = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()

                val comment = Comment(
                    id = commentId,
                    recipeId = recipeId ?: "",  // Use empty string instead of null
                    blogId = blogId ?: "",      // Use empty string instead of null
                    userId = userId,
                    userName = userName,
                    userProfileImage = userProfileImage,
                    content = content,
                    timestamp = timestamp,
                    likes = 0,
                    likedBy = listOf()  // Initialize empty list
                )

                val success = commentRepository.addComment(comment)

                if (success) {
                    // Refresh comments list
                    if (recipeId != null && recipeId.isNotEmpty()) {
                        loadCommentsByRecipeId(recipeId)
                    }
                    if (blogId != null && blogId.isNotEmpty()) {
                        loadCommentsByBlogId(blogId)
                    }
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to add comment"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error adding comment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteComment(commentId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val comment = commentRepository.getCommentById(commentId)

                if (comment == null) {
                    _errorMessage.value = "Comment not found"
                    _isLoading.value = false
                    return@launch
                }

                val success = commentRepository.deleteComment(commentId, userId)

                if (success) {
                    // Refresh comments list
                    comment.recipeId?.let { loadCommentsByRecipeId(it) }
                    comment.blogId?.let { loadCommentsByBlogId(it) }
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to delete comment"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting comment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likeComment(commentId: String, userId: String) {
        viewModelScope.launch {
            try {
                val success = commentRepository.likeComment(commentId, userId)

                if (success) {
                    // Update the comment in the list
                    val updatedComment = commentRepository.getCommentById(commentId)
                    if (updatedComment != null) {
                        val currentList = _comments.value.toMutableList()
                        val index = currentList.indexOfFirst { it.id == commentId }
                        if (index != -1) {
                            currentList[index] = updatedComment
                            _comments.value = currentList
                        }
                    }
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to like comment"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error liking comment: ${e.message}"
            }
        }
    }

    fun unlikeComment(commentId: String, userId: String) {
        viewModelScope.launch {
            try {
                val success = commentRepository.unlikeComment(commentId, userId)

                if (success) {
                    // Update the comment in the list
                    val updatedComment = commentRepository.getCommentById(commentId)
                    if (updatedComment != null) {
                        val currentList = _comments.value.toMutableList()
                        val index = currentList.indexOfFirst { it.id == commentId }
                        if (index != -1) {
                            currentList[index] = updatedComment
                            _comments.value = currentList
                        }
                    }
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to unlike comment"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error unliking comment: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
