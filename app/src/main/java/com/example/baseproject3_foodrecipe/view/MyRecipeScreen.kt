package com.example.baseproject3_foodrecipe.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import com.example.baseproject3_foodrecipe.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecipesScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val userRecipes by userViewModel.userRecipes.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    // Selected recipe for deletion
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load user recipes
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            userViewModel.loadUserRecipes(user.id)
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && selectedRecipe != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete '${selectedRecipe?.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedRecipe?.let { recipe ->
                            recipeViewModel.deleteRecipe(recipe.id)
                            // Refresh the list
                            currentUser?.let { user ->
                                userViewModel.loadUserRecipes(user.id)
                            }
                        }
                        showDeleteDialog = false
                        selectedRecipe = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Recipes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    currentUser?.let { user ->
                        navController.navigate("create_recipe/${user.id}/${user.name}")
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Recipe")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (userRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Recipes Yet",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Create your first recipe by tapping the + button",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            currentUser?.let { user ->
                                navController.navigate("create_recipe/${user.id}/${user.name}")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Recipe")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                items(userRecipes) { recipe ->
                    Box {
                        RecipeCard(
                            title = recipe.name,
                            cookingTime = "${recipe.totalTime} min",
                            difficulty = recipe.difficulty,
                            imageRes = R.drawable.italian_pasta, // Replace with actual image
                            onClick = { navController.navigate("recipe_detail/${recipe.id}") }
                        )

                        // Edit and delete buttons
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    // Navigate to edit screen (not implemented yet)
                                    // navController.navigate("edit_recipe/${recipe.id}")
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        shape = MaterialTheme.shapes.small
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = {
                                    selectedRecipe = recipe
                                    showDeleteDialog = true
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        shape = MaterialTheme.shapes.small
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
