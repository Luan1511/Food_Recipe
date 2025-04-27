package com.example.baseproject3_foodrecipe.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CommentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val commentsCollection = db.collection("comments")

    suspend fun getCommentsByRecipe(recipeId: String): List<Comment> {
        return try {
            withContext(Dispatchers.IO) {
                val snapshot = commentsCollection
                    .whereEqualTo("recipeId", recipeId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Comment::class.java)
                    } catch (e: Exception) {
                        Log.e("CommentRepository", "Error converting document to Comment: ${e.message}")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CommentRepository", "Error getting comments: ${e.message}")
            emptyList()
        }
    }

    suspend fun addComment(comment: Comment): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                commentsCollection.document(comment.id).set(comment).await()
                true
            }
        } catch (e: Exception) {
            Log.e("CommentRepository", "Error adding comment: ${e.message}")
            false
        }
    }

    suspend fun deleteComment(commentId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                commentsCollection.document(commentId).delete().await()
                true
            }
        } catch (e: Exception) {
            Log.e("CommentRepository", "Error deleting comment: ${e.message}")
            false
        }
    }

    suspend fun likeComment(commentId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val commentDoc = commentsCollection.document(commentId).get().await()
                val currentLikes = commentDoc.getLong("likes") ?: 0
                commentsCollection.document(commentId).update("likes", currentLikes + 1).await()
                true
            }
        } catch (e: Exception) {
            Log.e("CommentRepository", "Error liking comment: ${e.message}")
            false
        }
    }
}
