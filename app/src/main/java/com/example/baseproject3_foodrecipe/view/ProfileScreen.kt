package com.example.baseproject3_foodrecipe.view

import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.model.BlogPost
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
    val userRecipes by userViewModel.userRecipes.collectAsState()
    val userBlogs by blogViewModel.userBlogs.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()
    val currentAuthUser = authViewModel.currentUser.collectAsState().value
    val isCurrentUserProfile = currentAuthUser?.uid == userId
    val isAdmin by authViewModel.isAdmin.collectAsState()

    // State for follow button
    var isFollowing by remember { mutableStateOf(false) }
    var followButtonEnabled by remember { mutableStateOf(true) }

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
            userViewModel.loadUserRecipes(userId)
            blogViewModel.loadUserBlogs(userId)
        }
    }

    // Debug: Print userId and currentAuthUser to console
    LaunchedEffect(userId, currentAuthUser) {
        println("ProfileScreen - userId: $userId")
        println("ProfileScreen - currentAuthUser: ${currentAuthUser?.uid}")
        println("ProfileScreen - isCurrentUserProfile: $isCurrentUserProfile")
    }

    // Load profile image
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (user.profileImageUrl.isNotEmpty()) {
                try {
                    profileBitmap = LocalImageStorage.loadImage(context, user.profileImageUrl)
                } catch (e: Exception) {
                    // Handle error loading image
                    println("Error loading profile image: ${e.message}")
                }
            }
        }
    }

    // Check if current user is following this user
    LaunchedEffect(currentUser, currentAuthUser) {
        if (currentUser != null && currentAuthUser != null && !isCurrentUserProfile && userId.isNotEmpty()) {
            isFollowing = userViewModel.isFollowingUser(currentAuthUser.uid, userId)
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

                    // Admin actions
                    if (isAdmin) {
                        IconButton(
                            onClick = {
                                // Navigate to admin panel
                                navController.navigate("admin_panel")
                            }
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Panel")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isCurrentUserProfile) {
                if (selectedTab == 0) {
                    // Recipe FAB
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("create_recipe")
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Recipe")
                    }
                } else {
                    // Blog FAB
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("create_blog/${currentAuthUser?.uid}/${currentAuthUser?.displayName ?: "User"}")
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Blog")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        // Debug: Print more information
                        println("Retrying getUserById with userId: $userId")
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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Image
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // User Name
                        Text(
                            text = currentUser?.name ?: "Người dùng",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        if (currentUser?.isChef == true) {
                            Text(
                                text = currentUser?.chefTitle ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Admin badge
                        if (currentUser?.isAdmin == true) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    "Admin",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bio
                        Text(
                            text = currentUser?.bio ?: "Chưa có thông tin giới thiệu",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Công thức
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = userRecipes.size.toString(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Công thức",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            }

                            // Bài viết
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = userBlogs.size.toString(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Bài viết",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            }

                            // Người theo dõi
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = currentUser?.followers?.size?.toString() ?: "0",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Người theo dõi",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            }

                            // Đang theo dõi
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = currentUser?.following?.size?.toString() ?: "0",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Đang theo dõi",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Edit Profile Button (only for current user) or Follow Button
                        if (isCurrentUserProfile) {
                            OutlinedButton(
                                onClick = { navController.navigate("edit_profile") },
                                modifier = Modifier.width(200.dp)
                            ) {
                                Text("Chỉnh sửa hồ sơ")
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (currentAuthUser != null) {
                                        followButtonEnabled = false
                                        coroutineScope.launch {
                                            val success = if (isFollowing) {
                                                userViewModel.unfollowUser(currentAuthUser.uid, userId)
                                            } else {
                                                userViewModel.followUser(currentAuthUser.uid, userId)
                                            }

                                            if (success) {
                                                isFollowing = !isFollowing
                                                val message = if (isFollowing) "Now following ${currentUser!!.name}" else "Unfollowed ${currentUser!!.name}"
                                                snackbarHostState.showSnackbar(message)
                                            } else {
                                                snackbarHostState.showSnackbar("Failed to update follow status")
                                            }
                                            followButtonEnabled = true
                                        }
                                    }
                                },
                                modifier = Modifier.width(200.dp),
                                colors = if (isFollowing) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    ButtonDefaults.buttonColors()
                                }
                            ) {
                                Icon(
                                    if (isFollowing) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFollowing) "Đang theo dõi" else "Theo dõi")
                            }
                        }

                        // Admin Panel Button (only for admins)
                        if (isAdmin) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { navController.navigate("admin_panel") },
                                modifier = Modifier.width(200.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Admin Panel")
                            }
                        }
                    }

                    Divider()
                }

                // Tabs
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
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
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        items(userRecipes.chunked(2)) { recipeRow ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                recipeRow.forEach { recipe ->
                                    RecipeCardFromModel(
                                        recipe = recipe,
                                        onClick = { navController.navigate("recipe_detail/${recipe.id}") },
                                        modifier = Modifier.weight(1f),
                                        onDeleteClick = if (isCurrentUserProfile || isAdmin) {
                                            {
                                                if (isAdmin) {
                                                    recipeViewModel.adminDeleteRecipe(recipe.id)
                                                } else {
                                                    currentAuthUser?.let { user ->
                                                        recipeViewModel.deleteRecipe(recipe.id)
                                                    }
                                                }
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
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isCurrentUserProfile)
                                            "Bạn chưa tạo bài viết nào. Hãy bắt đầu viết bài đầu tiên!"
                                        else
                                            "Người dùng này chưa tạo bài viết nào.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        items(userBlogs) { blog ->
                            BlogItem(
                                blog = blog,
                                isOwner = isCurrentUserProfile,
                                isAdmin = isAdmin,
                                onBlogClick = { navController.navigate("blog_detail/${blog.id}") },
                                onEditClick = { navController.navigate("edit_blog/${blog.id}") },
                                onDeleteClick = {
                                    coroutineScope.launch {
                                        if (isAdmin) {
                                            blogViewModel.adminDeleteBlogPost(blog.id)
                                        } else {
                                            currentAuthUser?.let { user ->
                                                blogViewModel.deleteBlogPost(blog.id, user.uid)
                                            }
                                        }
                                        blogViewModel.loadUserBlogs(userId)
                                        snackbarHostState.showSnackbar("Blog post deleted")
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun BlogItem(
    blog: BlogPost,
    isOwner: Boolean,
    isAdmin: Boolean,
    onBlogClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    var blogBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load blog image
    LaunchedEffect(blog.imageUrl) {
        if (blog.imageUrl.isNotEmpty()) {
            try {
                blogBitmap = LocalImageStorage.loadImage(context, blog.imageUrl)
            } catch (e: Exception) {
                // Handle error loading image
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onBlogClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Blog image
            if (blogBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Image(
                        bitmap = blogBitmap!!.asImageBitmap(),
                        contentDescription = blog.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.italian_pasta),
                        contentDescription = blog.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = blog.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Summary
                Text(
                    text = blog.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Info row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Author and date
                    Column {
                        Text(
                            text = "By ${blog.authorName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${blog.readTime} min read",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Actions for owner or admin
                    if (isOwner || isAdmin) {
                        Row {
                            if (isOwner) {
                                IconButton(onClick = onEditClick) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(onClick = onDeleteClick) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
