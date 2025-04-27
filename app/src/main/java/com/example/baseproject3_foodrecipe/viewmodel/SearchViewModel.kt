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

class SearchViewModel : ViewModel() {
    private val recipeRepository = RecipeRepository()
    private val userRepository = UserRepository()

    private val _searchResults = MutableStateFlow<SearchResults>(SearchResults())
    val searchResults: StateFlow<SearchResults> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = SearchResults()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Search recipes
                val recipes = recipeRepository.searchRecipes(query)

                // Search users
                val users = userRepository.searchUsers(query)

                _searchResults.value = SearchResults(
                    recipes = recipes,
                    users = users
                )
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Search failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = SearchResults()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

data class SearchResults(
    val recipes: List<Recipe> = emptyList(),
    val users: List<User> = emptyList()
)
