package com.example.baseproject3_foodrecipe.model

import java.util.UUID

data class Rating(
//    val id: String = UUID.randomUUID().toString(),
//    val recipeId: String = "",
//    val userId: String = "",
//    val value: Double = 0.0,
//    val timestamp: Long = System.currentTimeMillis(),
//    val userName: String = "",
//    val comment: String = "",
//    val blogId: String? = null,


    val id: String = "",
    val recipeId: String? = null,
    val blogId: String? = null,
    val userId: String = "",
    val userName: String = "",
    val value: Double = 0.0,
    val comment: String = "",
    val timestamp: Long = 0
)
