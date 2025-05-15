package com.example.baseproject3_foodrecipe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.MealItem
import com.example.baseproject3_foodrecipe.model.MealPlan
import com.example.baseproject3_foodrecipe.model.MealPlanRepository
import com.example.baseproject3_foodrecipe.model.MealType
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.model.UserRepository
import com.example.baseproject3_foodrecipe.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MealPlanViewModel(
    private val mealPlanRepository: MealPlanRepository = MealPlanRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _currentMealPlan = MutableStateFlow<MealPlan?>(null)
    val currentMealPlan: StateFlow<MealPlan?> = _currentMealPlan.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    companion object {
        val DEFAULT_MEAL_TIMES = mapOf(
            MealType.BREAKFAST to "8:00 AM",
            MealType.LUNCH to "1:00 PM",
            MealType.DINNER to "7:00 PM",
            MealType.SNACK to "4:00 PM"
        )
    }

    /**
     * Set the selected date and load meal plan for that date
     */
    fun setSelectedDate(date: Date) {
        viewModelScope.launch {
            _selectedDate.value = date
            loadMealPlanForDate(date)
        }
    }

    /**
     * Load the meal plan for a specific date
     */
    fun loadMealPlanForDate(date: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = getCurrentUserId()
                if (userId.isNotEmpty()) {
                    // First try to get all meal plans for this user
                    val allMealPlans = mealPlanRepository.getMealPlansFirebase(userId)

                    // Find the meal plan for this date
                    val mealPlan = allMealPlans.find { DateUtils.isSameDay(it.date, date) }

                    if (mealPlan != null) {
                        _currentMealPlan.value = mealPlan
                    } else {
                        // Create an empty meal plan for this date
                        _currentMealPlan.value = MealPlan(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            date = date,
                            breakfast = null,
                            lunch = null,
                            dinner = null,
                            snacks = emptyList()
                        )
                    }
                } else {
                    _errorMessage.value = "User not logged in"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading meal plan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a meal to the meal plan
     */
    fun addMealToMealPlan(date: Date, mealType: MealType, mealItem: MealItem) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = getCurrentUserId()
                if (userId.isNotEmpty()) {
                    // First check if a meal plan already exists for this date
                    var mealPlan = mealPlanRepository.getMealPlanForDate(userId, date)

                    if (mealPlan == null) {
                        // Create a new meal plan
                        mealPlan = MealPlan(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            date = date,
                            breakfast = if (mealType == MealType.BREAKFAST) mealItem else null,
                            lunch = if (mealType == MealType.LUNCH) mealItem else null,
                            dinner = if (mealType == MealType.DINNER) mealItem else null,
                            snacks = if (mealType == MealType.SNACK) listOf(mealItem) else emptyList()
                        )

                        // Save to Firebase
                        mealPlanRepository.addMealPlanFirebase(mealPlan)
                    } else {
                        // Update existing meal plan
                        val updatedMealPlan = when (mealType) {
                            MealType.BREAKFAST -> mealPlan.copy(breakfast = mealItem)
                            MealType.LUNCH -> mealPlan.copy(lunch = mealItem)
                            MealType.DINNER -> mealPlan.copy(dinner = mealItem)
                            MealType.SNACK -> {
                                val updatedSnacks = mealPlan.snacks.toMutableList()
                                updatedSnacks.add(mealItem)
                                mealPlan.copy(snacks = updatedSnacks)
                            }
                        }

                        // Save to Firebase
                        mealPlanRepository.updateMealPlanFirebase(updatedMealPlan)
                    }

                    // Update local state
                    _currentMealPlan.value = mealPlan
                } else {
                    _errorMessage.value = "User not logged in"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error adding meal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Remove a meal from the meal plan
     */
    fun removeMealFromMealPlan(mealType: MealType) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = getCurrentUserId()
                if (userId.isNotEmpty()) {
                    mealPlanRepository.removeMealFromMealPlan(userId, _selectedDate.value, mealType)
                    // Reload meal plan
                    loadMealPlanForDate(_selectedDate.value)
                } else {
                    _errorMessage.value = "User not logged in"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error removing meal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Remove a snack from the meal plan
     */
    fun removeSnackFromMealPlan(snackId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = getCurrentUserId()
                if (userId.isNotEmpty()) {
                    mealPlanRepository.removeSnackFromMealPlan(userId, _selectedDate.value, snackId)
                    // Reload meal plan
                    loadMealPlanForDate(_selectedDate.value)
                } else {
                    _errorMessage.value = "User not logged in"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error removing snack: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear any error messages
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Get the current user ID
     */
    private fun getCurrentUserId(): String {
        return userRepository.getCurrentUser()?.id ?: ""
    }
}
