package com.example.baseproject3_foodrecipe.model

import java.util.UUID

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userImageUrl: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0
)
