package com.example.baseproject3_foodrecipe.model

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val profileImageUrl: String = "",
    val bio: String = "",
    val followers: Int = 0,
    val following: Int = 0,
    val recipes: List<String> = emptyList(), // List of recipe IDs
    val savedRecipes: List<String> = emptyList(), // List of saved recipe IDs
    val chef: Boolean = false, // Use only this field, not isChef
    val chefTitle: String = "",
    val joinDate: Long = System.currentTimeMillis()
) {
    // Remove any getter that might cause conflicts
    // DO NOT add isChef() getter
}
