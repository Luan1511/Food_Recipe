package com.example.baseproject3_foodrecipe.model

data class MealItem(
    val id: String,
    val recipeId: String,
    val recipeName: String,
    val recipeImageUrl: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val tags: List<String> = emptyList(),
    val time: String = ""
)
