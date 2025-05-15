package com.example.baseproject3_foodrecipe.model

import java.util.UUID

data class BlogPost(
    val lastUpdated: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val views: Int = 0,
    val categories: List<String> = listOf(),
    val comments: Int = 0,
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val summary: String = "",
    val imageUrl: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val category: String = "",
    val readTime: Int = 5,
    val publishDate: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val featured: Boolean = false,
    val likes: Int = 0,
    val commentCount: Int = 0,
    val likedBy: List<String> = listOf()
)
