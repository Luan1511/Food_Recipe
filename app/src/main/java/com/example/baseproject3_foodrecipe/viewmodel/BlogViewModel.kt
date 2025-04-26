package com.example.baseproject3_foodrecipe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.BlogPost
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class BlogViewModel : ViewModel() {
    private val _blogPosts = MutableStateFlow<List<BlogPost>>(emptyList())
    val blogPosts: StateFlow<List<BlogPost>> = _blogPosts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadBlogPosts() {
        viewModelScope.launch {
            _isLoading.value = true

            // Simulate network delay
            delay(1000)

            // Create sample blog posts
            val samplePosts = listOf(
                BlogPost(
                    id = UUID.randomUUID().toString(),
                    title = "10 Healthy Breakfast Ideas for Busy Mornings",
                    summary = "Quick and nutritious breakfast recipes that will keep you energized throughout the day...",
                    authorName = "Sarah Johnson",
                    date = "March 15, 2025",
                    readTime = 5,
                    categories = listOf("Healthy", "Breakfast"),
                    featured = true
                ),
                BlogPost(
                    id = UUID.randomUUID().toString(),
                    title = "Perfect Pasta Recipes for Date Night",
                    summary = "Impress your special someone with these romantic pasta dishes...",
                    authorName = "Chef Michael",
                    date = "March 10, 2025",
                    readTime = 3,
                    categories = listOf("Italian", "Dinner")
                ),
                BlogPost(
                    id = UUID.randomUUID().toString(),
                    title = "Smoothie Bowl Art: A Visual Guide",
                    summary = "Learn how to create Instagram-worthy smoothie bowls...",
                    authorName = "Emma Wilson",
                    date = "March 8, 2025",
                    readTime = 4,
                    categories = listOf("Healthy", "Breakfast")
                ),
                BlogPost(
                    id = UUID.randomUUID().toString(),
                    title = "Vegan Taco Tuesday Ideas",
                    summary = "Delicious plant-based alternatives for your taco night...",
                    authorName = "Alex Green",
                    date = "March 5, 2025",
                    readTime = 5,
                    categories = listOf("Vegan", "Mexican")
                ),
                BlogPost(
                    id = UUID.randomUUID().toString(),
                    title = "5 Ways to Use Leftover Rice",
                    summary = "Transform yesterday's rice into delicious new meals...",
                    authorName = "Chef Michael",
                    date = "March 1, 2025",
                    readTime = 3,
                    categories = listOf("Tips", "Budget")
                ),
                BlogPost(
                    id = UUID.randomUUID().toString(),
                    title = "The Art of Sourdough Bread",
                    summary = "Master the techniques for perfect homemade sourdough...",
                    authorName = "Sarah Johnson",
                    date = "February 25, 2025",
                    readTime = 7,
                    categories = listOf("Baking", "Bread")
                )
            )

            _blogPosts.value = samplePosts
            _isLoading.value = false
        }
    }
}
