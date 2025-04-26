package com.example.baseproject3_foodrecipe.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.ui.theme.*
import com.example.baseproject3_foodrecipe.viewmodel.AuthViewModel
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import com.example.baseproject3_foodrecipe.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    recipeViewModel: RecipeViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val featuredRecipes by recipeViewModel.featuredRecipes.collectAsState()
    val popularRecipes by recipeViewModel.popularRecipes.collectAsState()
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    val firebaseUser by authViewModel.currentUser.collectAsState()

    // Kiểm tra nếu chưa đăng nhập thì chuyển đến màn hình đăng nhập
    LaunchedEffect(firebaseUser) {
        if (firebaseUser == null) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // Load recipes if empty
    LaunchedEffect(Unit) {
        if (featuredRecipes.isEmpty()) {
            recipeViewModel.loadAllRecipes()
        }
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
                title = {
                    Text(
                        "CookLive",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Search action */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                    }
                    IconButton(onClick = { /* TODO: Notifications action */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Thông báo")
                    }
                    IconButton(onClick = { showLogoutDialog.value = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Đăng xuất")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Menu action */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ") },
                    selected = true,
                    onClick = { /* Already on home */ }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_video), contentDescription = "Video") },
                    label = { Text("Video") },
                    selected = false,
                    onClick = { /* TODO: Navigate to videos */ }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_saved), contentDescription = "Đã lưu") },
                    label = { Text("Đã lưu") },
                    selected = false,
                    onClick = { /* TODO: Navigate to saved */ }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_profile), contentDescription = "Hồ sơ") },
                    label = { Text("Hồ sơ") },
                    selected = false,
                    onClick = {
                        firebaseUser?.uid?.let { userId ->
                            navController.navigate("profile/$userId")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    firebaseUser?.let { user ->
                        navController.navigate("create_recipe/${user.uid}/${user.displayName}")
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo công thức")
            }
        }
    ) { paddingValues ->
        if (isLoading && featuredRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            HomeContent(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                featuredRecipes = featuredRecipes,
                popularRecipes = popularRecipes
            )
        }
    }
}
