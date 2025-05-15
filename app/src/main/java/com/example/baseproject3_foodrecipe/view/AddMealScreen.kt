package com.example.baseproject3_foodrecipe.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.baseproject3_foodrecipe.model.MealItem
import com.example.baseproject3_foodrecipe.model.MealType
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import com.example.baseproject3_foodrecipe.viewmodel.MealPlanViewModel
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    navController: NavController,
    date: String,
    mealType: String,
    mealPlanViewModel: MealPlanViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val allRecipes by recipeViewModel.recipes.collectAsState(initial = emptyList())
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val errorMessage by recipeViewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }

    val filteredRecipes = remember(searchQuery, allRecipes) {
        if (searchQuery.isBlank()) {
            allRecipes
        } else {
            allRecipes.filter { recipe ->
                recipe.name.contains(searchQuery, ignoreCase = true) ||
                        recipe.categories.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    val mealTypeEnum = remember(mealType) {
        try {
            MealType.valueOf(mealType)
        } catch (e: Exception) {
            MealType.BREAKFAST
        }
    }

    val dateObj = remember(date) {
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    LaunchedEffect(Unit) {
        recipeViewModel.loadAllRecipes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add ${mealTypeEnum.displayName}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search recipes") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredRecipes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recipes found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRecipes) { recipe ->
                        RecipeItemForMeal(
                            recipe = recipe,
                            onClick = {
                                val mealItem = MealItem(
                                    id = UUID.randomUUID().toString(),
                                    recipeName = recipe.name,
                                    recipeId = recipe.id,
                                    recipeImageUrl = recipe.imageUrl,
                                    calories = recipe.calories ?: 0,
                                    protein = recipe.nutritionInfo?.protein ?: 0,
                                    carbs = recipe.nutritionInfo?.carbs ?: 0,
                                    fat = recipe.nutritionInfo?.fat ?: 0,
                                    tags = recipe.categories
                                )

                                coroutineScope.launch {
                                    mealPlanViewModel.addMealToMealPlan(
                                        date = dateObj,
                                        mealType = mealTypeEnum,
                                        mealItem = mealItem
                                    )
                                    navController.popBackStack()
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Show error message if any
        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { recipeViewModel.clearErrorMessage() },
                title = { Text("Error") },
                text = { Text(errorMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = { recipeViewModel.clearErrorMessage() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun RecipeItemForMeal(
    recipe: Recipe,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var recipeBitmap by remember { mutableStateOf<Any?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(recipe.imageUrl) {
        if (recipe.imageUrl.isNotEmpty()) {
            coroutineScope.launch {
                if (LocalImageStorage.fileExists(context, recipe.imageUrl)) {
                    recipeBitmap = LocalImageStorage.loadImage(context, recipe.imageUrl)
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                        contentDescription = recipe.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recipe.name.take(1),
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
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${recipe.calories ?: 0} calories • ${recipe.cookTime} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    recipe.categories.take(2).forEach { tag ->
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
        }
    }
}
