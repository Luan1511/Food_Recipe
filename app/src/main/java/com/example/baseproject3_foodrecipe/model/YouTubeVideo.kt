package com.example.baseproject3_foodrecipe.model

data class YouTubeVideo(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val channelTitle: String = "",
    val publishedAt: String = "",
    val viewCount: String = "0",
    val likeCount: String = "0"
) {
    fun getShortDescription(): String {
        return if (description.length > 100) {
            description.substring(0, 97) + "..."
        } else {
            description
        }
    }
}
