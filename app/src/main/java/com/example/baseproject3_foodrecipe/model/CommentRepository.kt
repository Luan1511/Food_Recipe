package com.example.baseproject3_foodrecipe.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CommentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val commentsCollection = db.collection("comments")

    suspend fun getCommentsForRecipe(recipeId: String): List<Comment> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("recipeId", recipeId)
                .orderBy("timestamp")
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(Comment::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCommentsForBlog(blogId: String): List<Comment> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("blogId", blogId)
                .orderBy("timestamp")
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(Comment::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addComment(comment: Comment): Boolean {
        return try {
            commentsCollection.document(comment.id).set(comment).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteComment(commentId: String): Boolean {
        return try {
            commentsCollection.document(commentId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateComment(commentId: String, newContent: String): Boolean {
        return try {
            commentsCollection.document(commentId)
                .update("content", newContent)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCommentById(commentId: String): Comment? {
        return try {
            val document = commentsCollection.document(commentId).get().await()
            document.toObject(Comment::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCommentsByUser(userId: String): List<Comment> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(Comment::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCommentsByRecipeId(recipeId: String): List<Comment> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("recipeId", recipeId)
                .orderBy("timestamp")
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(Comment::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCommentsByBlogId(blogId: String): List<Comment> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("blogId", blogId)
                .orderBy("timestamp")
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(Comment::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun likeComment(commentId: String, userId: String): Boolean {
        return try {
            val comment = getCommentById(commentId)

            if (comment != null && !comment.likedBy.contains(userId)) {
                val updatedLikedBy = comment.likedBy + userId
                val updatedLikes = comment.likes + 1

                commentsCollection.document(commentId)
                    .update(
                        mapOf(
                            "likedBy" to updatedLikedBy,
                            "likes" to updatedLikes
                        )
                    )
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun unlikeComment(commentId: String, userId: String): Boolean {
        return try {
            val comment = getCommentById(commentId)

            if (comment != null && comment.likedBy.contains(userId)) {
                val updatedLikedBy = comment.likedBy.filter { it != userId }
                val updatedLikes = (comment.likes - 1).coerceAtLeast(0)

                commentsCollection.document(commentId)
                    .update(
                        mapOf(
                            "likedBy" to updatedLikedBy,
                            "likes" to updatedLikes
                        )
                    )
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteComment(commentId: String, userId: String): Boolean {
        return try {
            val comment = getCommentById(commentId)

            if (comment != null && (comment.userId == userId)) {
                commentsCollection.document(commentId).delete().await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
