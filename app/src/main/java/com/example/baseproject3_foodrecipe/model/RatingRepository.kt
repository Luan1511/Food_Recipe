package com.example.baseproject3_foodrecipe.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RatingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ratingsCollection = db.collection("ratings")

    // Get user rating for a recipe
    suspend fun getUserRatingForRecipe(userId: String, recipeId: String): Rating? {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("recipeId", recipeId)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isEmpty()) {
                null
            } else {
                snapshot.documents[0].toObject(Rating::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    // Get user rating for a blog
    suspend fun getUserRatingForBlog(userId: String, blogId: String): Rating? {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("blogId", blogId)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isEmpty()) {
                null
            } else {
                snapshot.documents[0].toObject(Rating::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    // Get average rating for a recipe
    suspend fun getAverageRatingForRecipe(recipeId: String): Pair<Float, Int> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            val ratings = snapshot.documents.mapNotNull { document ->
                document.toObject(Rating::class.java)
            }

            if (ratings.isEmpty()) {
                Pair(0.0f, 0)
            } else {
                val average = ratings.sumOf { it.value } / ratings.size
                Pair(average.toFloat(), ratings.size)
            }
        } catch (e: Exception) {
            Pair(0.0f, 0)
        }
    }

    // Get average rating for a blog
    suspend fun getAverageRatingForBlog(blogId: String): Pair<Float, Int> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("blogId", blogId)
                .get()
                .await()

            val ratings = snapshot.documents.mapNotNull { document ->
                document.toObject(Rating::class.java)
            }

            if (ratings.isEmpty()) {
                Pair(0.0f, 0)
            } else {
                val average = ratings.sumOf { it.value } / ratings.size
                Pair(average.toFloat(), ratings.size)
            }
        } catch (e: Exception) {
            Pair(0.0f, 0)
        }
    }

    // Add a new rating
    suspend fun addRating(rating: Rating): Boolean {
        return try {
            // Check if user already rated this item
            val existingRating = if (rating.recipeId != null && rating.recipeId.isNotEmpty()) {
                getUserRatingForRecipe(rating.userId, rating.recipeId)
            } else if (rating.blogId != null && rating.blogId.isNotEmpty()) {
                getUserRatingForBlog(rating.userId, rating.blogId)
            } else {
                null
            }

            if (existingRating != null) {
                // Update existing rating instead
                return updateRating(rating.copy(id = existingRating.id))
            }

            ratingsCollection.document(rating.id).set(rating).await()

            // Update average rating on the item
            updateItemAverageRating(rating)

            true
        } catch (e: Exception) {
            false
        }
    }

    // Update an existing rating
    suspend fun updateRating(rating: Rating): Boolean {
        return try {
            ratingsCollection.document(rating.id).set(rating).await()

            // Update average rating on the item
            updateItemAverageRating(rating)

            true
        } catch (e: Exception) {
            false
        }
    }

    // Delete a rating
    suspend fun deleteRating(ratingId: String): Boolean {
        return try {
            // Get the rating first to know which item to update
            val rating = getRatingById(ratingId)

            if (rating != null) {
                ratingsCollection.document(ratingId).delete().await()

                // Update average rating on the item
                updateItemAverageRating(rating)

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Delete a rating with user verification
    suspend fun deleteRating(ratingId: String, userId: String): Boolean {
        return try {
            val rating = getRatingById(ratingId)

            if (rating != null && rating.userId == userId) {
                ratingsCollection.document(ratingId).delete().await()

                // Update average rating on the item
                updateItemAverageRating(rating)

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Get a rating by ID
    suspend fun getRatingById(ratingId: String): Rating? {
        return try {
            val document = ratingsCollection.document(ratingId).get().await()
            document.toObject(Rating::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Update the average rating on the rated item (recipe or blog)
    private suspend fun updateItemAverageRating(rating: Rating) {
        try {
            if (rating.recipeId != null && rating.recipeId.isNotEmpty()) {
                val (average, count) = getAverageRatingForRecipe(rating.recipeId)
                db.collection("recipes").document(rating.recipeId).update(
                    mapOf(
                        "averageRating" to average,
                        "ratingCount" to count
                    )
                ).await()
            } else if (rating.blogId != null && rating.blogId.isNotEmpty()) {
                val (average, count) = getAverageRatingForBlog(rating.blogId)
                db.collection("blogs").document(rating.blogId).update(
                    mapOf(
                        "averageRating" to average,
                        "ratingCount" to count
                    )
                ).await()
            }
        } catch (e: Exception) {
            // Log error but don't fail the operation
            println("Error updating item average rating: ${e.message}")
        }
    }

    suspend fun getRatingsForRecipe(recipeId: String): List<Rating> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(Rating::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRatingsForBlog(blogId: String): List<Rating> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("blogId", blogId)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(Rating::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserRatings(userId: String): List<Rating> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(Rating::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
