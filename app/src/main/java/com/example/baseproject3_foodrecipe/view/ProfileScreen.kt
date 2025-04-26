package com.example.baseproject3_foodrecipe.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String,
    userViewModel: UserViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val userRecipes by userViewModel.userRecipes.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val isCurrentUserProfile = authViewModel.currentUser.collectAsState().value?.uid == userId

    // Load user data
    LaunchedEffect(userId) {
        userViewModel.getUserById(userId)
    }

    val showLogoutDialog = remember { mutableStateOf(false) }

    if (showLogoutDialog.value) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog.value = false },
            title = { Text("Đăng xuất") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.logoutUser()
                        showLogoutDialog.value = false
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                ) {
                    Text("Đăng xuất")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog.value = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isCurrentUserProfile) {
                        IconButton(onClick = { showLogoutDialog.value = true }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Đăng xuất")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isCurrentUserProfile) {
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
        }
    ) { paddingValues ->
        if (isLoading || currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            ProfileContent(
                user = currentUser!!,
                recipes = userRecipes,
                modifier = Modifier.padding(paddingValues),
                onRecipeClick = { recipeId ->
                    navController.navigate("recipe_detail/$recipeId")
                },
                isCurrentUserProfile = isCurrentUserProfile
            )
        }
    }
}

@Composable
fun ProfileContent(
    user: User,
    recipes: List<Recipe>,
    modifier: Modifier = Modifier,
    onRecipeClick: (String) -> Unit,
    isCurrentUserProfile: Boolean
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        // Profile Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image
                Image(
                    painter = painterResource(id = R.drawable.chef_avatar), // Replace with actual image
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                // User Name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                if (user.chef) {
                    Text(
                        text = user.chefTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bio
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = recipes.size.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Công thức",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = user.followers.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Người theo dõi",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = user.following.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Đang theo dõi",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Profile Button (only for current user)
                if (isCurrentUserProfile) {
                    OutlinedButton(
                        onClick = { /* TODO: Edit Profile */ },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text("Chỉnh sửa hồ sơ")
                    }
                } else {
                    Button(
                        onClick = { /* TODO: Follow user */ },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text("Theo dõi")
                    }
                }
            }

            Divider()
        }

        // Recipes Section
        item {
            Text(
                text = "Công thức của tôi",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(16.dp)
            )
        }

        if (recipes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCurrentUserProfile)
                            "Bạn chưa tạo công thức nào. Hãy bắt đầu tạo công thức đầu tiên!"
                        else
                            "Người dùng này chưa tạo công thức nào.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(recipes.chunked(2)) { recipeRow ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recipeRow.forEach { recipe ->
                        RecipeCard(
                            title = recipe.name,
                            cookingTime = "${recipe.totalTime} phút",
                            difficulty = recipe.difficulty,
                            imageRes = R.drawable.italian_pasta, // Replace with actual image
                            modifier = Modifier.weight(1f),
                            onClick = { onRecipeClick(recipe.id) }
                        )
                    }

                    // If odd number of recipes, add an empty space
                    if (recipeRow.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
