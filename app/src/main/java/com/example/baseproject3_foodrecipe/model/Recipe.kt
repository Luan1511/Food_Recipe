package com.example.baseproject3_foodrecipe.model

import java.util.UUID

data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorImageUrl: String = "",
    val prepTime: Int = 0, // in minutes
    val cookTime: Int = 0, // in minutes
    val totalTime: Int = 0, // in minutes
    val servings: Int = 0,
    val difficulty: String = "Easy", // Easy, Medium, Hard
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val calories: Int = 0,
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val cuisine: String = "",
    val dateCreated: Long = System.currentTimeMillis(),
    val published: Boolean = true // Use only this field, not isPublished
) {
    // Remove any getter that might cause conflicts
    // DO NOT add isPublished() getter
}
