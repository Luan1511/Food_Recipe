package com.example.baseproject3_foodrecipe.view.admins

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.baseproject3_foodrecipe.model.BlogPost
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "RecipeListScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesManageScreen(
    navController: NavController,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val recipes by recipeViewModel.recipes.collectAsState(initial = emptyList())
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val errorMessage by recipeViewModel.errorMessage.collectAsState()

    // Load recipes when the screen is first displayed
    LaunchedEffect(Unit) {
        Log.d(TAG, "Loading recipes")
        recipeViewModel.getAllRecipes()
    }

    // Show error message if any
    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            Log.e(TAG, "Error: $errorMessage")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Recipes") },
                actions = {
                    IconButton(onClick = {
                        Log.d(TAG, "Create recipe button clicked")
                        navController.navigate("create_recipe")
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Blog")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d(TAG, "Create recipe FAB clicked")
                    navController.navigate("create_recipe")
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Blog")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (recipes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Article,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No recipe posts yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Be the first to create a recipe post!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            Log.d(TAG, "Create first recipe button clicked")
                            navController.navigate("create_recipe")
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Blog Post")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recipes) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = {
                                navController.navigate("recipe_detail/${recipe.id}")
                            }
                        )
                        Button(
                            onClick = { recipeViewModel.deleteRecipe(recipe.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFCC2F23)
                            ),
                            shape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp)
                        ) {
                            Text(
                                text = "Delete",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Add some space at the bottom for the FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Format date
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = remember(recipe.creationDate) {
        dateFormat.format(Date(recipe.creationDate))
    }

    // For local image loading
    var recipeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load local image if path exists
    LaunchedEffect(recipe.imageUrl) {
        if (recipe.imageUrl.isNotEmpty()) {
            try {
                Log.d(TAG, "Loading image from path: ${recipe.imageUrl}")
                val bitmap = LocalImageStorage.loadImage(context, recipe.imageUrl)
                recipeBitmap = bitmap
                Log.d(TAG, "Image loaded successfully: ${bitmap != null}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image: ${e.message}", e)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Blog image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.LightGray)
            ) {
                if (recipeBitmap != null) {
                    Image(
                        bitmap = recipeBitmap!!.asImageBitmap(),
                        contentDescription = recipe.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder - just use a Box with an icon instead of painterResource
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }
                }

                // Category chip
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = recipe.difficulty,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Blog content
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = recipe.description.take(100) + if (recipe.description.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Author and date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "By ${recipe.authorName}",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
