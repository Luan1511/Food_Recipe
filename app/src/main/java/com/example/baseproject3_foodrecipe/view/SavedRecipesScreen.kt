package com.example.baseproject3_foodrecipe.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkRemove
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
import com.example.baseproject3_foodrecipe.model.User
import com.example.baseproject3_foodrecipe.viewmodel.AuthViewModel
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import com.example.baseproject3_foodrecipe.viewmodel.UserViewModel
import kotlinx.coroutines.launch

private const val TAG = "SavedRecipesScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRecipesScreen(
    navController: NavController,
    recipeViewModel: RecipeViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val firebaseUser = authViewModel.currentUser.collectAsState().value
    val currentUser = remember(firebaseUser) {
        firebaseUser?.let { userViewModel.getUserId(it.uid) }
    }
    val userState by userViewModel.currentUser.collectAsState()

    var savedRecipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Track if we've attempted to load recipes
    var hasAttemptedLoad by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            savedRecipes = recipeViewModel.getUserSavedRecipe(currentUser.id)
            Log.e(TAG, "Saved Recipe Counter: " + savedRecipes.count().toString())
        }
    }

    LaunchedEffect(Unit) {
        Log.d(TAG, "Initial LaunchedEffect - checking current user")
    }

    // Once we have the auth user, get the full user profile if needed
    LaunchedEffect(currentUser) {
        Log.d(TAG, "Auth user changed: ${currentUser?.id}")
        currentUser?.id?.let { uid ->
            if (userState == null) {
                Log.d(TAG, "Getting user profile for UID: $uid")
                userViewModel.getUserById(uid)
            }
        }
    }

    // Load saved recipes once we have a valid user
    LaunchedEffect(currentUser, userState) {
        if (!hasAttemptedLoad && currentUser != null) {
            Log.d(TAG, "Loading saved recipes for user: ${currentUser?.id}")
            currentUser?.id?.let { userId ->
                try {
                    recipeViewModel.loadSavedRecipes(userId)
                    hasAttemptedLoad = true
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading saved recipes: ${e.message}", e)
                    snackbarHostState.showSnackbar("Failed to load saved recipes: ${e.message}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Recipes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (currentUser == null) {
            // User not logged in
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
                    Text(
                        text = "Please log in to view saved recipes",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { navController.navigate("login") }
                    ) {
                        Text("Log In")
                    }
                }
            }
        } else if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (savedRecipes.isEmpty()) {
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
                        Icons.Default.BookmarkRemove,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Saved Recipes",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Recipes you save will appear here",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { navController.navigate("home") }
                    ) {
                        Text("Browse Recipes")
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
                items(savedRecipes) { recipe ->
                    RecipeCardFromModel(
                        recipe = recipe,
                        onClick = {
                            Log.d(TAG, "Navigating to recipe detail: ${recipe.id}")
                            navController.navigate("recipe_detail/${recipe.id}")
                        },
                        onLongClick = {
                            coroutineScope.launch {
                                try {
                                    currentUser?.id?.let { userId ->
                                        Log.d(TAG, "Removing recipe from bookmarks: ${recipe.id}")
                                        recipeViewModel.unbookmarkRecipe(recipe.id, userId)
                                        snackbarHostState.showSnackbar("Recipe removed from saved")
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error removing bookmark: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Failed to remove bookmark: ${e.message}")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}