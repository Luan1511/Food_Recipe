package com.example.baseproject3_foodrecipe.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val recipeCount: Int = 0,
    val blogCount: Int = 0,
    val savedRecipes: List<String> = listOf(),
    val followingUsers: List<String> = listOf(),
    val isChef: Boolean = false,
    val chefTitle: String = "",
    val isAdmin: Boolean = false, // Thêm trường isAdmin với giá trị mặc định là false
    val username: String = "",
    val followerUsers: List<String> = emptyList(),

)
