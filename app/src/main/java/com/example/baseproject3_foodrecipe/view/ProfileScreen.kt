package com.example.baseproject3_foodrecipe.view

import android.graphics.Bitmap
import android.util.Log
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
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import com.example.baseproject3_foodrecipe.viewmodel.AuthViewModel
import com.example.baseproject3_foodrecipe.viewmodel.BlogViewModel
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
    authViewModel: AuthViewModel = viewModel(),
    blogViewModel: BlogViewModel = viewModel()
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val userRecipes by recipeViewModel.userRecipes.collectAsState()
    val userBlogs by blogViewModel.userBlogs.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()
    val currentAuthUser = authViewModel.currentUser.collectAsState().value
    val isCurrentUserProfile = currentAuthUser?.uid == userId

    // State for tab selection
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Recipes", "Blogs")

    // Snackbar for notifications
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // For profile image
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load user data
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
            recipeViewModel.loadUserRecipes(userId)
            blogViewModel.loadUserBlogs(userId)
//            userRecipes = recipeViewModel.
        }
    }

    // Load profile image
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (user.profileImageUrl.isNotEmpty()) {
                try {
                    profileBitmap = LocalImageStorage.loadImage(context, user.profileImageUrl)
                } catch (e: Exception) {
                    println("Error loading profile image: ${e.message}")
                }
            }
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

    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
                userViewModel.clearError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (isCurrentUserProfile) {
                when (selectedTab) {
                    0 -> {
                        FloatingActionButton(
                            onClick = {
                                navController.navigate("create_recipe")
                            },
                            containerColor = Color(0xFFFF7043),
                            contentColor = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Recipe"
                            )
                        }
                    }
                    1 -> {
                        FloatingActionButton(
                            onClick = {
                                currentAuthUser?.let { user ->
                                    navController.navigate("create_blog/${user.uid}/${user.displayName ?: "User"}")
                                }
                            },
                            containerColor = Color(0xFFFF7043),
                            contentColor = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Blog"
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (userId.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Vui lòng đăng nhập để xem hồ sơ")
            }
        } else if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFA000))
            }
        } else if (currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Không tìm thấy thông tin người dùng")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        userViewModel.getUserById(userId)
                    }) {
                        Text("Thử lại")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Profile Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Profile Title
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Profile Image and Name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Profile Image with Camera Icon
                            Box(contentAlignment = Alignment.BottomEnd) {
                                if (profileBitmap != null) {
                                    Image(
                                        bitmap = profileBitmap!!.asImageBitmap(),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.chef_avatar),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // Camera Icon
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2196F3))
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CameraAlt,
                                        contentDescription = "Change Profile Picture",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Name and Title
                            Column {
                                Text(
                                    text = currentUser?.name ?: "Sarah Wilson",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                Text(
                                    text = if (currentUser?.isChef == true)
                                        currentUser?.chefTitle ?: "Food enthusiast & Recipe creator"
                                    else
                                        "Food enthusiast & Recipe creator",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Divider()
                }

                // Stats Section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Recipes
                        StatItem(
                            count = userRecipes.size,
                            label = "Recipes"
                        )

                        // Blogs
                        StatItem(
                            count = userBlogs.size,
                            label = "Blogs"
                        )

                        // Followers
                        StatItem(
                            count = currentUser?.followers?.size ?: 0,
//                            count = 1,
                            label = "Followers",
                            isLargeNumber = true
                        )

                        // Following
                        StatItem(
//                            count = 11,
                            count = currentUser?.following?.size ?: 0,
                            label = "Following"
                        )
                    }

                    Divider()
                }

                if (!isCurrentUserProfile) {
                    item {
                        Button(
                            onClick = { userViewModel.currentUser.value?.let {
                                userViewModel.followUser(
                                    it.id, userId)
                            } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Follow",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                // Account Settings
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ACCOUNT SETTINGS",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Personal Information
                        SettingsItem(
                            icon = Icons.Outlined.Person,
                            title = "Personal Information",
                            onClick = { navController.navigate("edit_profile") }
                        )

                        // Scan Refrigerator
                        SettingsItem(
                            icon = Icons.Outlined.CameraAlt,
                            title = "Scan Refrigerator",
                            onClick = { navController.navigate("refrigerator_scan") }
                        )

                        // Problem Report
                        SettingsItem(
                            icon = Icons.Outlined.Report,
                            title = "Problem Report",
                            onClick = { /* Navigate to problem report */ }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Log Out Button
                        Button(
                            onClick = { showLogoutDialog.value = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF7043)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Log Out",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Divider()
                }

                // Tabs
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color(0xFFFFF8E1),
                        contentColor = Color(0xFFFF7043),
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                height = 3.dp,
                                color = Color(0xFFFF7043)
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }

                // Content based on selected tab
                when (selectedTab) {
                    0 -> {
                        // Recipes Tab
                        item {
                            if (userRecipes.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Bạn chưa tạo công thức nào. Hãy bắt đầu tạo công thức đầu tiên!",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Add refresh button
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    println("Manual refresh: Loading recipes for user ID: $userId")
                                                    recipeViewModel.loadUserRecipes(userId)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Refresh"
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Refresh")
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Display recipes in a grid (2 columns)
                        items(userRecipes.chunked(2)) { recipeRow ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                recipeRow.forEach { recipe ->
                                    Log.e("TAG", "Image URL: " + recipe.imageUrl)
                                    RecipeCardFromModel(
                                        recipe = recipe,
                                        onClick = { navController.navigate("recipe_detail/${recipe.id}") },
                                        modifier = Modifier.weight(1f),
                                        onLongClick = if (isCurrentUserProfile) {
                                            {
                                                recipeViewModel.deleteRecipe(recipe.id)
                                                // Refresh recipes after deletion
                                                recipeViewModel.loadUserRecipes(userId)
                                            }
                                        } else null
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
                    1 -> {
                        // Blogs Tab
                        item {
                            if (userBlogs.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Bạn chưa tạo bài viết nào. Hãy bắt đầu viết bài đầu tiên!",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Display blogs in a list
                        items(userBlogs) { blog ->
                            BlogItem(
                                blog = blog,
                                isOwner = isCurrentUserProfile,
                                isAdmin = false, // We'll handle admin status separately
                                onBlogClick = { navController.navigate("blog_detail/${blog.id}") },
                                onEditClick = { navController.navigate("edit_blog/${blog.id}") },
                                onDeleteClick = {
                                    coroutineScope.launch {
                                        currentAuthUser?.let { user ->
                                            blogViewModel.deleteBlogPost(blog.id, user.uid)
                                            // Refresh blogs after deletion
                                            blogViewModel.loadUserBlogs(userId)
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun StatItem(count: Int, label: String, isLargeNumber: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isLargeNumber && count > 1000)
                String.format("%.1fK", count / 1000.0)
            else
                count.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.DarkGray
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}
