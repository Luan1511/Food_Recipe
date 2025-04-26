package com.example.baseproject3_foodrecipe.model

import java.util.UUID

data class BlogPost(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val summary: String = "",
    val imageUrl: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorImageUrl: String = "",
    val date: String = "",
    val readTime: Int = 0,
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val featured: Boolean = false,
    val published: Boolean = true,
    val dateCreated: Long = System.currentTimeMillis()
)
