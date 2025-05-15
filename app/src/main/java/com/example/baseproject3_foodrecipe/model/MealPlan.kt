package com.example.baseproject3_foodrecipe.model

import java.util.Date

data class MealPlan(
    val id: String,
    val userId: String = "",
    val date: Date,
    val breakfast: MealItem? = null,
    val lunch: MealItem? = null,
    val dinner: MealItem? = null,
    val snacks: List<MealItem> = emptyList()
) {
    // Computed properties for nutrition totals
    val totalCalories: Int
        get() {
            var total = 0
            breakfast?.let { total += it.calories }
            lunch?.let { total += it.calories }
            dinner?.let { total += it.calories }
            snacks.forEach { total += it.calories }
            return total
        }

    val totalProtein: Int
        get() {
            var total = 0
            breakfast?.let { total += it.protein }
            lunch?.let { total += it.protein }
            dinner?.let { total += it.protein }
            snacks.forEach { total += it.protein }
            return total
        }

    val totalCarbs: Int
        get() {
            var total = 0
            breakfast?.let { total += it.carbs }
            lunch?.let { total += it.carbs }
            dinner?.let { total += it.carbs }
            snacks.forEach { total += it.carbs }
            return total
        }

    val totalFat: Int
        get() {
            var total = 0
            breakfast?.let { total += it.fat }
            lunch?.let { total += it.fat }
            dinner?.let { total += it.fat }
            snacks.forEach { total += it.fat }
            return total
        }
}
