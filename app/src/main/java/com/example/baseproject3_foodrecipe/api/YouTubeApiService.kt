package com.example.baseproject3_foodrecipe.api

import android.util.Log
import com.example.baseproject3_foodrecipe.model.YouTubeVideo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class YouTubeApiService {
    private val apiKey = "AIzaSyDsFeiKA6qB8dbSg7xbHEp69jnJoPqOcss" // Replace with your actual API key
    private val baseUrl = "https://www.googleapis.com/youtube/v3"
    private val db = FirebaseFirestore.getInstance()
    private val savedVideosCollection = db.collection("savedVideos")

    suspend fun getTrendingVideos(category: String = "cooking"): List<YouTubeVideo> = withContext(Dispatchers.IO) {
        try {
            val encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8.toString())
            val url = "$baseUrl/search?part=snippet&maxResults=10&q=$encodedCategory&type=video&key=$apiKey"
            val response = URL(url).readText()
            parseSearchResults(response)
        } catch (e: Exception) {
            // Return some mock data for testing when API key is not set or there's an error
            getMockCookingVideos()
        }
    }

    suspend fun searchVideos(query: String): List<YouTubeVideo> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val url = "$baseUrl/search?part=snippet&maxResults=20&q=$encodedQuery&type=video&key=$apiKey"
            val response = URL(url).readText()
            parseSearchResults(response)
        } catch (e: Exception) {
            // Return filtered mock data based on query
            getMockCookingVideos().filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
    }

    suspend fun getVideoDetails(videoId: String): YouTubeVideo = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/videos?part=snippet,statistics&id=$videoId&key=$apiKey"
            val response = URL(url).readText()
            parseVideoDetails(response)
        } catch (e: Exception) {
            // Return a mock video with the requested ID
            getMockCookingVideos().find { it.id == videoId } ?: YouTubeVideo(
                id = videoId,
                title = "How to Make Perfect Pasta",
                description = "Learn the secrets to making perfect pasta every time with this detailed tutorial.",
                thumbnailUrl = "https://i.ytimg.com/vi/$videoId/mqdefault.jpg",
                channelTitle = "Cooking Master",
                publishedAt = "2023-01-15T12:00:00Z",
                viewCount = "125000",
                likeCount = "8500"
            )
        }
    }

    suspend fun getRelatedVideos(videoId: String): List<YouTubeVideo> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/search?part=snippet&maxResults=10&relatedToVideoId=$videoId&type=video&key=$apiKey"
            val response = URL(url).readText()
            parseSearchResults(response)
        } catch (e: Exception) {
            // Return some mock related videos
            getMockCookingVideos().shuffled().take(5)
        }
    }

    // Lưu video vào Firestore để có thể quản lý (thêm/xóa)
    suspend fun saveVideo(video: YouTubeVideo): Boolean = withContext(Dispatchers.IO) {
        try {
            savedVideosCollection.document(video.id).set(video).await()
            true
        } catch (e: Exception) {
            Log.e("YouTubeApiService", "Error saving video: ${e.message}")
            false
        }
    }

    // Xóa video (chỉ xóa khỏi danh sách đã lưu, không thể xóa video từ YouTube)
    suspend fun deleteVideo(videoId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            savedVideosCollection.document(videoId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("YouTubeApiService", "Error deleting video: ${e.message}")
            false
        }
    }

    // Admin có thể xóa bất kỳ video nào đã lưu
    suspend fun adminDeleteVideo(videoId: String): Boolean {
        return deleteVideo(videoId)
    }

    // Lấy danh sách video đã lưu
    suspend fun getSavedVideos(): List<YouTubeVideo> = withContext(Dispatchers.IO) {
        try {
            val snapshot = savedVideosCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(YouTubeVideo::class.java) }
        } catch (e: Exception) {
            Log.e("YouTubeApiService", "Error getting saved videos: ${e.message}")
            emptyList()
        }
    }

    fun extractVideoIdFromUrl(url: String): String {
        return when {
            url.contains("youtube.com/watch") -> {
                val regex = "(?:v=)([^&]+)".toRegex()
                regex.find(url)?.groupValues?.get(1) ?: ""
            }
            url.contains("youtu.be/") -> {
                url.substringAfterLast("/")
            }
            else -> url
        }
    }

    private fun parseSearchResults(jsonString: String): List<YouTubeVideo> {
        val videos = mutableListOf<YouTubeVideo>()
        val jsonObject = JSONObject(jsonString)
        val items = jsonObject.getJSONArray("items")

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val id = item.getJSONObject("id").getString("videoId")
            val snippet = item.getJSONObject("snippet")

            val video = YouTubeVideo(
                id = id,
                title = snippet.getString("title"),
                description = snippet.getString("description"),
                thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url"),
                channelTitle = snippet.getString("channelTitle"),
                publishedAt = snippet.getString("publishedAt"),
                viewCount = "0",
                likeCount = "0"
            )

            videos.add(video)
        }

        return videos
    }

    private fun parseVideoDetails(jsonString: String): YouTubeVideo {
        val jsonObject = JSONObject(jsonString)
        val items = jsonObject.getJSONArray("items")

        if (items.length() == 0) {
            return YouTubeVideo()
        }

        val item = items.getJSONObject(0)
        val id = item.getString("id")
        val snippet = item.getJSONObject("snippet")
        val statistics = item.getJSONObject("statistics")

        return YouTubeVideo(
            id = id,
            title = snippet.getString("title"),
            description = snippet.getString("description"),
            thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url"),
            channelTitle = snippet.getString("channelTitle"),
            publishedAt = snippet.getString("publishedAt"),
            viewCount = statistics.optString("viewCount", "0"),
            likeCount = statistics.optString("likeCount", "0")
        )
    }

    // Mock data for testing when API key is not set
    private fun getMockCookingVideos(): List<YouTubeVideo> {
        return listOf(
            YouTubeVideo(
                id = "dJz2w4x6Yv8",
                title = "The Perfect Homemade Pizza",
                description = "Learn how to make restaurant-quality pizza at home with simple ingredients.",
                thumbnailUrl = "https://i.ytimg.com/vi/dJz2w4x6Yv8/mqdefault.jpg",
                channelTitle = "Cooking Master",
                publishedAt = "2023-05-10T14:30:00Z",
                viewCount = "245000",
                likeCount = "18500"
            ),
            YouTubeVideo(
                id = "8Ve3pV1j0Kg",
                title = "Easy 15-Minute Pasta Recipes",
                description = "Quick and delicious pasta recipes for busy weeknights.",
                thumbnailUrl = "https://i.ytimg.com/vi/8Ve3pV1j0Kg/mqdefault.jpg",
                channelTitle = "Quick Meals",
                publishedAt = "2023-04-22T10:15:00Z",
                viewCount = "189000",
                likeCount = "12300"
            ),
            YouTubeVideo(
                id = "rX3YvJUFm5c",
                title = "Authentic Thai Curry from Scratch",
                description = "Master the art of making authentic Thai curry paste and create amazing curries at home.",
                thumbnailUrl = "https://i.ytimg.com/vi/rX3YvJUFm5c/mqdefault.jpg",
                channelTitle = "Global Cuisine",
                publishedAt = "2023-03-15T09:45:00Z",
                viewCount = "320000",
                likeCount = "24500"
            ),
            YouTubeVideo(
                id = "pLVk2LjRfcU",
                title = "Perfect Chocolate Cake Recipe",
                description = "The ultimate chocolate cake recipe that's moist, rich, and incredibly delicious.",
                thumbnailUrl = "https://i.ytimg.com/vi/pLVk2LjRfcU/mqdefault.jpg",
                channelTitle = "Baking Heaven",
                publishedAt = "2023-02-28T16:20:00Z",
                viewCount = "415000",
                likeCount = "32800"
            ),
            YouTubeVideo(
                id = "qB3D4jCZ2-Y",
                title = "Homemade Sushi for Beginners",
                description = "Step-by-step guide to making beautiful sushi rolls at home with simple tools.",
                thumbnailUrl = "https://i.ytimg.com/vi/qB3D4jCZ2-Y/mqdefault.jpg",
                channelTitle = "Sushi Master",
                publishedAt = "2023-01-18T11:30:00Z",
                viewCount = "278000",
                likeCount = "19700"
            ),
            YouTubeVideo(
                id = "kH87ynWlMVg",
                title = "French Croissants from Scratch",
                description = "Learn the secrets to making flaky, buttery croissants at home like a professional baker.",
                thumbnailUrl = "https://i.ytimg.com/vi/kH87ynWlMVg/mqdefault.jpg",
                channelTitle = "Pastry Chef",
                publishedAt = "2022-12-05T08:45:00Z",
                viewCount = "356000",
                likeCount = "28900"
            ),
            YouTubeVideo(
                id = "tR9oULw5jXc",
                title = "5-Ingredient Weeknight Dinners",
                description = "Simple and delicious dinner recipes using just 5 ingredients.",
                thumbnailUrl = "https://i.ytimg.com/vi/tR9oULw5jXc/mqdefault.jpg",
                channelTitle = "Simple Cooking",
                publishedAt = "2022-11-20T15:10:00Z",
                viewCount = "198000",
                likeCount = "15600"
            ),
            YouTubeVideo(
                id = "vH9qMV3yRxs",
                title = "Homemade Sourdough Bread Masterclass",
                description = "Complete guide to making amazing sourdough bread from starter to finished loaf.",
                thumbnailUrl = "https://i.ytimg.com/vi/vH9qMV3yRxs/mqdefault.jpg",
                channelTitle = "Bread Expert",
                publishedAt = "2022-10-12T13:25:00Z",
                viewCount = "425000",
                likeCount = "34200"
            ),
            YouTubeVideo(
                id = "bF7Rg1YaQ1k",
                title = "Korean BBQ at Home",
                description = "How to set up and cook an authentic Korean BBQ feast in your own kitchen.",
                thumbnailUrl = "https://i.ytimg.com/vi/bF7Rg1YaQ1k/mqdefault.jpg",
                channelTitle = "Korean Cuisine",
                publishedAt = "2022-09-08T17:40:00Z",
                viewCount = "312000",
                likeCount = "25800"
            ),
            YouTubeVideo(
                id = "p4JD91hofiY",
                title = "Vegetarian Buddha Bowls 5 Ways",
                description = "Five delicious and nutritious vegetarian buddha bowl recipes for healthy eating.",
                thumbnailUrl = "https://i.ytimg.com/vi/p4JD91hofiY/mqdefault.jpg",
                channelTitle = "Healthy Eats",
                publishedAt = "2022-08-15T12:50:00Z",
                viewCount = "267000",
                likeCount = "21400"
            )
        )
    }
}
