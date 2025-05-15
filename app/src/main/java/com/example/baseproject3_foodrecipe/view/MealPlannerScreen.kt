package com.example.baseproject3_foodrecipe.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.baseproject3_foodrecipe.model.MealItem
import com.example.baseproject3_foodrecipe.model.MealType
import com.example.baseproject3_foodrecipe.utils.DateUtils
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import com.example.baseproject3_foodrecipe.viewmodel.MealPlanViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerScreen(
    navController: NavController,
    mealPlanViewModel: MealPlanViewModel = viewModel()
) {
    val currentMealPlan by mealPlanViewModel.currentMealPlan.collectAsState()
    val selectedDate by mealPlanViewModel.selectedDate.collectAsState()
    val isLoading by mealPlanViewModel.isLoading.collectAsState()
    val errorMessage by mealPlanViewModel.errorMessage.collectAsState()

    val calendar = remember { Calendar.getInstance() }
    calendar.time = selectedDate

    val currentMonth = remember(selectedDate) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate)
    }

    val weekDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    // Generate dates for the week
    val weekDates = remember(selectedDate) {
        val dates = mutableListOf<Date>()
        val tempCalendar = Calendar.getInstance()
        tempCalendar.time = selectedDate

        // Find the first day of the week (Monday)
        while (tempCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            tempCalendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        // Add 7 days starting from Monday
        repeat(7) {
            dates.add(tempCalendar.time)
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        dates
    }

    LaunchedEffect(selectedDate) {
        mealPlanViewModel.loadMealPlanForDate(selectedDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Planner") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month and year header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentMonth,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { /* Open calendar */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { /* Open filters */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Week day selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDates.forEachIndexed { index, date ->
                    val dayOfMonth = SimpleDateFormat("d", Locale.getDefault()).format(date)
                    val isSelected = DateUtils.isSameDay(date, selectedDate)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { mealPlanViewModel.setSelectedDate(date) }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .padding(vertical = 8.dp, horizontal = 14.dp)
                    ) {
                        Text(
                            text = weekDays[index],
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = dayOfMonth,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Breakfast
                    item {
                        MealSection(
                            mealType = MealType.BREAKFAST,
                            mealItem = currentMealPlan?.breakfast,
                            time = "8:00 AM",
                            onAddClick = {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val dateString = dateFormat.format(selectedDate)
                                navController.navigate("add_meal/${dateString}/${MealType.BREAKFAST.name}")
                            },
                            onRemoveClick = {
                                mealPlanViewModel.removeMealFromMealPlan(MealType.BREAKFAST)
                            }
                        )
                    }

                    // Lunch
                    item {
                        MealSection(
                            mealType = MealType.LUNCH,
                            mealItem = currentMealPlan?.lunch,
                            time = "1:00 PM",
                            onAddClick = {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val dateString = dateFormat.format(selectedDate)
                                navController.navigate("add_meal/${dateString}/${MealType.LUNCH.name}")
                            },
                            onRemoveClick = {
                                mealPlanViewModel.removeMealFromMealPlan(MealType.LUNCH)
                            }
                        )
                    }

                    // Dinner
                    item {
                        MealSection(
                            mealType = MealType.DINNER,
                            mealItem = currentMealPlan?.dinner,
                            time = "7:00 PM",
                            onAddClick = {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val dateString = dateFormat.format(selectedDate)
                                navController.navigate("add_meal/${dateString}/${MealType.DINNER.name}")
                            },
                            onRemoveClick = {
                                mealPlanViewModel.removeMealFromMealPlan(MealType.DINNER)
                            }
                        )
                    }

                    // Snacks
                    item {
                        if (currentMealPlan?.snacks?.isNotEmpty() == true) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Snacks",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "4:00 PM",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                currentMealPlan?.snacks?.forEach { snack ->
                                    MealItemCard(
                                        mealItem = snack,
                                        onRemoveClick = {
                                            mealPlanViewModel.removeSnackFromMealPlan(snack.id)
                                        }
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val dateString = dateFormat.format(selectedDate)
                                        navController.navigate("add_meal/${dateString}/${MealType.SNACK.name}")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add snack")
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Snacks",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "4:00 PM",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val dateString = dateFormat.format(selectedDate)
                                        navController.navigate("add_meal/${dateString}/${MealType.SNACK.name}")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add snack")
                                }
                            }
                        }
                    }

                    // Spacer at the bottom
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Show error message if any
        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { mealPlanViewModel.clearError() },
                title = { Text("Error") },
                text = { Text(errorMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = { mealPlanViewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun MealSection(
    mealType: MealType,
    mealItem: MealItem?,
    time: String,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mealType.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = time,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (mealItem != null) {
            MealItemCard(mealItem = mealItem, onRemoveClick = onRemoveClick)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add ${mealType.displayName.lowercase()} recipe",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MealItemCard(
    mealItem: MealItem,
    onRemoveClick: () -> Unit
) {
    val context = LocalContext.current
    var recipeBitmap by remember { mutableStateOf<Any?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(mealItem.recipeImageUrl) {
        if (mealItem.recipeImageUrl.isNotEmpty()) {
            coroutineScope.launch {
                if (LocalImageStorage.fileExists(context, mealItem.recipeImageUrl)) {
                    recipeBitmap = LocalImageStorage.loadImage(context, mealItem.recipeImageUrl)
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recipe image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (recipeBitmap != null) {
                    AsyncImage(
                        model = recipeBitmap,
                        contentDescription = mealItem.recipeName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mealItem.recipeName.take(1),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Recipe details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mealItem.recipeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${mealItem.calories} calories",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    mealItem.tags.take(2).forEach { tag ->
                        val tagColor = when {
                            tag.contains("protein", ignoreCase = true) -> Color(0xFFFBE9BA)
                            tag.contains("healthy", ignoreCase = true) -> Color(0xFFD1F0D9)
                            tag.contains("carb", ignoreCase = true) -> Color(0xFFFBD9BA)
                            tag.contains("gluten", ignoreCase = true) -> Color(0xFFE9D9F0)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val textColor = when {
                            tag.contains("protein", ignoreCase = true) -> Color(0xFFAD5700)
                            tag.contains("healthy", ignoreCase = true) -> Color(0xFF00813F)
                            tag.contains("carb", ignoreCase = true) -> Color(0xFFAD3700)
                            tag.contains("gluten", ignoreCase = true) -> Color(0xFF6200AE)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = tagColor
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
