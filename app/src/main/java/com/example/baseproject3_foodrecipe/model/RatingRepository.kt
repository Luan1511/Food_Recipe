package com.example.baseproject3_foodrecipe.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RatingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ratingsCollection = db.collection("ratings")
    private val recipesCollection = db.collection("recipes")

    suspend fun getRatingsByRecipe(recipeId: String): List<Rating> {
        return try {
            withContext(Dispatchers.IO) {
                val snapshot = ratingsCollection
                    .whereEqualTo("recipeId", recipeId)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Rating::class.java)
                    } catch (e: Exception) {
                        Log.e("RatingRepository", "Error converting document to Rating: ${e.message}")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RatingRepository", "Error getting ratings: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserRating(recipeId: String, userId: String): Rating? {
        return try {
            withContext(Dispatchers.IO) {
                val snapshot = ratingsCollection
                    .whereEqualTo("recipeId", recipeId)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                if (snapshot.documents.isNotEmpty()) {
                    snapshot.documents[0].toObject(Rating::class.java)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("RatingRepository", "Error getting user rating: ${e.message}")
            null
        }
    }

    suspend fun addOrUpdateRating(rating: Rating): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Check if user already rated this recipe
                val existingRating = getUserRating(rating.recipeId, rating.userId)

                if (existingRating != null) {
                    // Update existing rating
                    ratingsCollection.document(existingRating.id).update("value", rating.value).await()
                } else {
                    // Add new rating
                    ratingsCollection.document(rating.id).set(rating).await()
                }

                // Update recipe's average rating
                updateRecipeRating(rating.recipeId)

                true
            }
        } catch (e: Exception) {
            Log.e("RatingRepository", "Error adding/updating rating: ${e.message}")
            false
        }
    }

    private suspend fun updateRecipeRating(recipeId: String) {
        try {
            val ratings = getRatingsByRecipe(recipeId)

            if (ratings.isNotEmpty()) {
                val averageRating = ratings.map { it.value }.average()
                val ratingCount = ratings.size

                recipesCollection.document(recipeId)
                    .update(
                        mapOf(
                            "rating" to averageRating,
                            "ratingCount" to ratingCount
                        )
                    )
                    .await()
            }
        } catch (e: Exception) {
            Log.e("RatingRepository", "Error updating recipe rating: ${e.message}")
        }
    }
}
