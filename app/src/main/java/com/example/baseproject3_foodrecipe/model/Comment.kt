package com.example.baseproject3_foodrecipe.model

import java.util.UUID

data class Comment(
    val userImageUrl: String = "",
    val id: String = "",
    val recipeId: String = "",
    val blogId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val likes: Int = 0,
    val likedBy: List<String> = listOf()
)
