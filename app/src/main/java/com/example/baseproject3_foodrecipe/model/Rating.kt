package com.example.baseproject3_foodrecipe.model

import java.util.UUID

data class Rating(
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String = "",
    val userId: String = "",
    val value: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
