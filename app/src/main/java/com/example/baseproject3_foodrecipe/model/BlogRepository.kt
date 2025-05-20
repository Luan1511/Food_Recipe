package com.example.baseproject3_foodrecipe.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

private const val TAG = "BlogRepository"

class BlogRepository {
    private val db = FirebaseFirestore.getInstance()
    private val blogsCollection = db.collection("blogs")

    suspend fun getAllBlogs(): List<BlogPost> {
        Log.d(TAG, "Getting all blogs")
        try {
            val snapshot = blogsCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val blogs = snapshot.toObjects(BlogPost::class.java)
            Log.d(TAG, "Retrieved ${blogs.size} blogs")
            return blogs
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all blogs: ${e.message}", e)
            throw e
        }
    }

    suspend fun getFeaturedBlogs(): List<BlogPost> {
        try {
            val snapshot = blogsCollection
                .whereEqualTo("featured", true)
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .get()
                .await()

            return snapshot.toObjects(BlogPost::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting featured blogs: ${e.message}", e)
            throw e
        }
    }

    suspend fun getBlogsByAuthor(authorId: String): List<BlogPost> {
        try {
            val snapshot = blogsCollection
                .whereEqualTo("authorId", authorId)
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .get()
                .await()

            return snapshot.toObjects(BlogPost::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting blogs by author: ${e.message}", e)
            throw e
        }
    }

    suspend fun getBlogsByCategory(category: String): List<BlogPost> {
        try {
            val snapshot = blogsCollection
                .whereEqualTo("category", category)
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .get()
                .await()

            return snapshot.toObjects(BlogPost::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting blogs by category: ${e.message}", e)
            throw e
        }
    }

    suspend fun getBlogById(blogId: String): BlogPost? {
        try {
            val document = blogsCollection.document(blogId).get().await()
            return document.toObject(BlogPost::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting blog by ID: ${e.message}", e)
            throw e
        }
    }

    /**
     * Create a new blog post
     */
    suspend fun createBlogPost(blog: BlogPost): Boolean {
        Log.d(TAG, "Creating blog post: ${blog.title}")
        try {
            // Use the blog's ID as the document ID
            blogsCollection.document(blog.id).set(blog).await()
            Log.d(TAG, "Blog post created successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating blog post: ${e.message}", e)
            throw e
        }
    }

    /**
     * Update a blog post
     */
    suspend fun updateBlogPost(blog: BlogPost): Boolean {
        try {
            blogsCollection.document(blog.id).set(blog).await()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating blog post: ${e.message}", e)
            throw e
        }
    }

    /**
     * Delete a blog post (admin function, no author verification)
     */
    suspend fun adminDeleteBlogPost(blogId: String): Boolean {
        try {
            blogsCollection.document(blogId).delete().await()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting blog post: ${e.message}", e)
            throw e
        }
    }

    /**
     * Delete a blog post (with author verification)
     */
    suspend fun deleteBlogPost(blogId: String, userId: String): Boolean {
        try {
            val document = blogsCollection.document(blogId).get().await()
            val blog = document.toObject(BlogPost::class.java)

            if (blog == null) {
                return false
            }

            if (blog.authorId != userId) {
                return false
            }

            blogsCollection.document(blogId).delete().await()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting blog post: ${e.message}", e)
            throw e
        }
    }

    /**
     * Delete a blog post (without author verification)
     */
    suspend fun deleteBlogPost(blogId: String): Boolean {
        try {
            blogsCollection.document(blogId).delete().await()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting blog post: ${e.message}", e)
            throw e
        }
    }

    /**
     * Like a blog post
     */
    suspend fun likeBlogPost(blogId: String, userId: String): Boolean {
        try {
            val document = blogsCollection.document(blogId).get().await()
            val blog = document.toObject(BlogPost::class.java)

            if (blog == null) {
                return false
            }

            val likedBy = blog.likedBy.toMutableList()

            if (userId in likedBy) {
                return true // Already liked
            }

            likedBy.add(userId)

            blogsCollection.document(blogId).update(
                mapOf(
                    "likedBy" to likedBy,
                    "likes" to likedBy.size
                )
            ).await()

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error liking blog post: ${e.message}", e)
            throw e
        }
    }

    /**
     * Unlike a blog post
     */
    suspend fun unlikeBlogPost(blogId: String, userId: String): Boolean {
        try {
            val document = blogsCollection.document(blogId).get().await()
            val blog = document.toObject(BlogPost::class.java)

            if (blog == null) {
                return false
            }

            val likedBy = blog.likedBy.toMutableList()

            if (userId !in likedBy) {
                return true // Already not liked
            }

            likedBy.remove(userId)

            blogsCollection.document(blogId).update(
                mapOf(
                    "likedBy" to likedBy,
                    "likes" to likedBy.size
                )
            ).await()

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error unliking blog post: ${e.message}", e)
            throw e
        }
    }
}
