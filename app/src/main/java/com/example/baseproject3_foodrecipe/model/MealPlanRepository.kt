package com.example.baseproject3_foodrecipe.model

import com.example.baseproject3_foodrecipe.utils.DateUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Repository for managing meal plans
 */
class MealPlanRepository {
    // In-memory storage for meal plans (replace with database in production)
    private val mealPlans = mutableMapOf<String, MutableList<MealPlan>>() // userId -> List<MealPlan>
    private val db = FirebaseFirestore.getInstance()
    private val mealPlansCollection = db.collection("mealPlans")

    /**
     * Get all meal plans for a user
     */
    suspend fun getMealPlans(userId: String): List<MealPlan> = withContext(Dispatchers.IO) {
        return@withContext mealPlans[userId]?.toList() ?: emptyList()
    }

    suspend fun getMealPlansFirebase(userId: String? = null): List<MealPlan> {
        return try {
            val query = if (userId != null) {
                mealPlansCollection.whereEqualTo("userId", userId)
            } else {
                mealPlansCollection
            }

            val snapshot = query.get().await()
            val mealPlans = mutableListOf<MealPlan>()

            for (document in snapshot.documents) {
                val id = document.id
                val userId = document.getString("userId") ?: ""

                // Handle date field properly - it could be a Timestamp, Date, or String
                val date = when (val dateField = document.get("date")) {
                    is Timestamp -> dateField.toDate()
                    is Date -> dateField
                    is String -> {
                        try {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateField) ?: Date()
                        } catch (e: Exception) {
                            Date() // Fallback to current date if parsing fails
                        }
                    }
                    else -> Date() // Default to current date if field is missing or of unknown type
                }

                // Get meal items
                val breakfastMap = document.get("breakfast") as? Map<String, Any>
                val lunchMap = document.get("lunch") as? Map<String, Any>
                val dinnerMap = document.get("dinner") as? Map<String, Any>
                val snacksList = document.get("snacks") as? List<Map<String, Any>> ?: emptyList()

                val breakfast = breakfastMap?.let { mapToMealItem(it) }
                val lunch = lunchMap?.let { mapToMealItem(it) }
                val dinner = dinnerMap?.let { mapToMealItem(it) }
                val snacks = snacksList.map { mapToMealItem(it) }

                val mealPlan = MealPlan(
                    id = id,
                    userId = userId,
                    date = date,
                    breakfast = breakfast,
                    lunch = lunch,
                    dinner = dinner,
                    snacks = snacks
                )

                mealPlans.add(mealPlan)
            }

            mealPlans
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Get a meal plan for a specific date
     */
    suspend fun getMealPlanForDate(userId: String, date: Date): MealPlan? = withContext(Dispatchers.IO) {
        val userMealPlans = mealPlans[userId] ?: return@withContext null

        return@withContext userMealPlans.find { mealPlan ->
            DateUtils.isSameDay(mealPlan.date, date)
        } ?: MealPlan(
            id = UUID.randomUUID().toString(),
            date = date,
            breakfast = null,
            lunch = null,
            dinner = null,
            snacks = emptyList()
        )
    }

    /**
     * Add a meal plan
     */
    suspend fun addMealPlan(userId: String, mealPlan: MealPlan): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!mealPlans.containsKey(userId)) {
                mealPlans[userId] = mutableListOf()
            }

            // Check if a meal plan for this date already exists
            val existingIndex = mealPlans[userId]?.indexOfFirst {
                DateUtils.isSameDay(it.date, mealPlan.date)
            } ?: -1

            if (existingIndex >= 0) {
                // Update existing meal plan
                mealPlans[userId]?.set(existingIndex, mealPlan)
            } else {
                // Add new meal plan
                mealPlans[userId]?.add(mealPlan)
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addMealPlanFirebase(mealPlan: MealPlan) {
        try {
            val mealPlanData = hashMapOf(
                "userId" to mealPlan.userId,
                "date" to mealPlan.date, // Store as Date object directly
                "breakfast" to mealPlan.breakfast?.let { mealItemToMap(it) },
                "lunch" to mealPlan.lunch?.let { mealItemToMap(it) },
                "dinner" to mealPlan.dinner?.let { mealItemToMap(it) },
                "snacks" to mealPlan.snacks.map { mealItemToMap(it) }
            )

            mealPlansCollection.document(mealPlan.id).set(mealPlanData).await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Update a meal plan
     */
    suspend fun updateMealPlan(userId: String, mealPlan: MealPlan): Boolean = withContext(Dispatchers.IO) {
        try {
            val userMealPlans = mealPlans[userId] ?: return@withContext false

            val index = userMealPlans.indexOfFirst { it.id == mealPlan.id }
            if (index >= 0) {
                userMealPlans[index] = mealPlan
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateMealPlanFirebase(mealPlan: MealPlan) {
        try {
            val mealPlanData = hashMapOf(
                "userId" to mealPlan.userId,
                "date" to mealPlan.date, // Store as Date object directly
                "breakfast" to mealPlan.breakfast?.let { mealItemToMap(it) },
                "lunch" to mealPlan.lunch?.let { mealItemToMap(it) },
                "dinner" to mealPlan.dinner?.let { mealItemToMap(it) },
                "snacks" to mealPlan.snacks.map { mealItemToMap(it) }
            )

            mealPlansCollection.document(mealPlan.id).set(mealPlanData).await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Delete a meal plan
     */
    suspend fun deleteMealPlan(userId: String, mealPlanId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userMealPlans = mealPlans[userId] ?: return@withContext false

            val removed = userMealPlans.removeIf { it.id == mealPlanId }
            removed
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteMealPlanFirebase(mealPlanId: String) {
        try {
            mealPlansCollection.document(mealPlanId).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Add a meal to a meal plan
     */
    suspend fun addMealToMealPlan(
        userId: String,
        date: Date,
        mealType: MealType,
        mealItem: MealItem
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Get or create meal plan for this date
            val mealPlan = getMealPlanForDate(userId, date)

            val updatedMealPlan = if (mealPlan != null) {
                when (mealType) {
                    MealType.BREAKFAST -> mealPlan.copy(breakfast = mealItem)
                    MealType.LUNCH -> mealPlan.copy(lunch = mealItem)
                    MealType.DINNER -> mealPlan.copy(dinner = mealItem)
                    MealType.SNACK -> {
                        val updatedSnacks = mealPlan.snacks.toMutableList()
                        updatedSnacks.add(mealItem)
                        mealPlan.copy(snacks = updatedSnacks)
                    }
                }
            } else {
                // Create new meal plan
                when (mealType) {
                    MealType.BREAKFAST -> MealPlan(
                        id = UUID.randomUUID().toString(),
                        date = date,
                        breakfast = mealItem,
                        lunch = null,
                        dinner = null,
                        snacks = emptyList()
                    )
                    MealType.LUNCH -> MealPlan(
                        id = UUID.randomUUID().toString(),
                        date = date,
                        breakfast = null,
                        lunch = mealItem,
                        dinner = null,
                        snacks = emptyList()
                    )
                    MealType.DINNER -> MealPlan(
                        id = UUID.randomUUID().toString(),
                        date = date,
                        breakfast = null,
                        lunch = null,
                        dinner = mealItem,
                        snacks = emptyList()
                    )
                    MealType.SNACK -> MealPlan(
                        id = UUID.randomUUID().toString(),
                        date = date,
                        breakfast = null,
                        lunch = null,
                        dinner = null,
                        snacks = listOf(mealItem)
                    )
                }
            }

            // Save the updated meal plan
            addMealPlan(userId, updatedMealPlan)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Remove a meal from a meal plan
     */
    suspend fun removeMealFromMealPlan(
        userId: String,
        date: Date,
        mealType: MealType
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val mealPlan = getMealPlanForDate(userId, date) ?: return@withContext false

            val updatedMealPlan = when (mealType) {
                MealType.BREAKFAST -> mealPlan.copy(breakfast = null)
                MealType.LUNCH -> mealPlan.copy(lunch = null)
                MealType.DINNER -> mealPlan.copy(dinner = null)
                MealType.SNACK -> return@withContext false // Cannot remove all snacks at once
            }

            // If the meal plan is empty after removing the meal, delete it
            if (updatedMealPlan.breakfast == null &&
                updatedMealPlan.lunch == null &&
                updatedMealPlan.dinner == null &&
                updatedMealPlan.snacks.isEmpty()) {
                deleteMealPlan(userId, updatedMealPlan.id)
            } else {
                updateMealPlan(userId, updatedMealPlan)
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Remove a snack from a meal plan
     */
    suspend fun removeSnackFromMealPlan(
        userId: String,
        date: Date,
        snackId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val mealPlan = getMealPlanForDate(userId, date) ?: return@withContext false

            val updatedSnacks = mealPlan.snacks.filter { it.id != snackId }
            val updatedMealPlan = mealPlan.copy(snacks = updatedSnacks)

            // If the meal plan is empty after removing the snack, delete it
            if (updatedMealPlan.breakfast == null &&
                updatedMealPlan.lunch == null &&
                updatedMealPlan.dinner == null &&
                updatedMealPlan.snacks.isEmpty()) {
                deleteMealPlan(userId, updatedMealPlan.id)
            } else {
                updateMealPlan(userId, updatedMealPlan)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun mapToMealItem(map: Map<String, Any>): MealItem {
        return MealItem(
            id = map["id"] as? String ?: UUID.randomUUID().toString(),
            recipeId = map["recipeId"] as? String ?: "",
            recipeName = map["recipeName"] as? String ?: "",
            recipeImageUrl = map["recipeImageUrl"] as? String ?: "",
            calories = (map["calories"] as? Number)?.toInt() ?: 0,
            protein = (map["protein"] as? Number)?.toInt() ?: 0,
            carbs = (map["carbs"] as? Number)?.toInt() ?: 0,
            fat = (map["fat"] as? Number)?.toInt() ?: 0,
            tags = map["tags"] as? List<String> ?: emptyList(),
            time = map["time"] as? String ?: ""
        )
    }

    private fun mealItemToMap(mealItem: MealItem): Map<String, Any> {
        return mapOf(
            "id" to mealItem.id,
            "recipeId" to mealItem.recipeId,
            "recipeName" to mealItem.recipeName,
            "recipeImageUrl" to mealItem.recipeImageUrl,
            "calories" to mealItem.calories,
            "protein" to mealItem.protein,
            "carbs" to mealItem.carbs,
            "fat" to mealItem.fat,
            "tags" to mealItem.tags,
            "time" to mealItem.time
        )
    }
}
