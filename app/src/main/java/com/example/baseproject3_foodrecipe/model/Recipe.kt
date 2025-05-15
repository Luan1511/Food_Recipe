package com.example.baseproject3_foodrecipe.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Data class representing a recipe
 */
data class Recipe(
    val creationDate: Long = 0,
    val nutritionInfo: NutritionInfo? = null,
    val isFeatured: Boolean = false,
    val isPopular: Boolean = false,

    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val prepTime: Int = 0,
    val cookTime: Int = 0,
    val servings: Int = 0,
    val difficulty: String = "Easy",
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val cuisine: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val reviewCount: Int = 0,
    val youtubeVideoId: String = "",

    // Use PropertyName annotation to map Firestore field names to properties
    @get:PropertyName("isFeatured")
    @set:PropertyName("isFeatured")
    var featured: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Data class for nutrition information
     */
    data class NutritionInfo(
        val calories: Int = 0,
        val protein: Int = 0,
        val carbs: Int = 0,
        val fat: Int = 0
    )

    constructor() : this(
        id = "",
        name = "",
        description = "",
        imageUrl = "",
        authorId = "",
        authorName = "",
        prepTime = 0,
        cookTime = 0,
        servings = 0,
        difficulty = "Easy",
        ingredients = emptyList(),
        instructions = emptyList(),
        categories = emptyList(),
        cuisine = "",
        calories = 0,
        protein = 0,
        carbs = 0,
        fat = 0,
        rating = 0.0,
        ratingCount = 0,
        reviewCount = 0,
        youtubeVideoId = "",
        featured = false,
        createdAt = System.currentTimeMillis()
    )

    val totalTime: Int
        get() = prepTime + cookTime
}
