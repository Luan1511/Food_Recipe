package com.example.baseproject3_foodrecipe.view

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.model.User
import com.example.baseproject3_foodrecipe.ui.theme.md_extended_rating
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import com.example.baseproject3_foodrecipe.viewmodel.AuthViewModel
import com.example.baseproject3_foodrecipe.viewmodel.CommentViewModel
import com.example.baseproject3_foodrecipe.viewmodel.RatingViewModel
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import com.example.baseproject3_foodrecipe.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "Recipe Screen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeId: String,
    recipeViewModel: RecipeViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    commentViewModel: CommentViewModel = viewModel(),
    ratingViewModel: RatingViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentRecipe by recipeViewModel.currentRecipe.collectAsState()
    val isLoading by recipeViewModel.isLoading.collectAsState()
    var currentUser: User? = null
    val comments by commentViewModel.comments.collectAsState()
    val userRating by ratingViewModel.userRating.collectAsState()

    var isBookmarked by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showCommentSection by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // For local image loading
    var recipeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load recipe data and check if it's bookmarked
    LaunchedEffect(recipeId, currentUser) {
        recipeViewModel.getRecipeById(recipeId)
        currentUser = authViewModel.currentUser.value?.let { userViewModel.getUserId(it.uid) }

        currentUser?.id?.let { userId ->
            try {
                // Check if recipe is bookmarked using the updated method
                isBookmarked = recipeViewModel.isRecipeSaved(recipeId, userId)
                ratingViewModel.getUserRatingForRecipe(userId, recipeId)
            } catch (e: Exception) {
                Log.e("RecipeDetailScreen", "Error checking bookmark status: ${e.message}", e)
            }
        }
        commentViewModel.loadCommentsByRecipeId(recipeId)
    }

    // Load local image if path exists
    LaunchedEffect(currentRecipe) {
        currentRecipe?.let { recipe ->
            if (recipe.imageUrl.isNotEmpty()) {
                try {
                    recipeBitmap = LocalImageStorage.loadImage(context, recipe.imageUrl)
                } catch (e: Exception) {
                    // Handle error loading image
                }
            }
        }
    }

    // Rating dialog
    if (showRatingDialog) {
        RatingDialog(
            currentRating = userRating?.value?.toDouble() ?: 0.0,
            onDismiss = { showRatingDialog = false },
            onRateRecipe = { rating ->
                val currentU = authViewModel.currentUser
                val user = currentU.value?.let { userViewModel.getUserId(it.uid) }

                user?.id?.let { userId ->
                    ratingViewModel.addRating(
                        recipeId = recipeId,
                        userId = userId,
                        userName = user?.name ?: "",
                        value = rating.toFloat(),
                        comment = ""
                    )
                    showRatingDialog = false
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val currentU = authViewModel.currentUser
                        val user = currentU.value?.let { userViewModel.getUserId(it.uid) }
                        user?.id?.let { userId ->
                            Log.d(TAG, "Toggling bookmark for recipe: $recipeId, user: $userId")
                            coroutineScope.launch {
                                try {
                                    if (isBookmarked) {
                                        // Use the updated unbookmark method that deletes from savedRecipes collection
                                        recipeViewModel.unbookmarkRecipe(recipeId, userId)
                                        isBookmarked = false
                                        snackbarHostState.showSnackbar("Recipe removed from saved")
                                    } else {
                                        // Use the updated bookmark method that adds to savedRecipes collection
                                        recipeViewModel.bookmarkRecipe(recipeId, userId)
                                        isBookmarked = true
                                        snackbarHostState.showSnackbar("Recipe saved")
                                    }
                                } catch (e: Exception) {
                                    Log.e("RecipeDetailScreen", "Error bookmarking: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Failed to update bookmark: ${e.message}")
                                }
                            }
                        } ?: run {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please log in to bookmark recipes")
                            }
                        }
                    }) {
                        Icon(
                            if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLoading || currentRecipe == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            RecipeDetailContent(
                recipe = currentRecipe!!,
                recipeBitmap = recipeBitmap,
                navController = navController,
                modifier = Modifier.padding(paddingValues),
                onCommentClick = { showCommentSection = !showCommentSection },
                onAuthorClick = { authorId ->
                    navController.navigate("profile/$authorId")
                },
                onRateClick = { showRatingDialog = true },
                userRating = userRating,
                comments = comments,
                showComments = showCommentSection,
                commentText = commentText,
                onCommentTextChange = { commentText = it },
                onAddComment = {
                    val currentU = authViewModel.currentUser
                    val user = currentU.value?.let { userViewModel.getUserId(it.uid) }
                    if (commentText.isNotBlank() && currentU != null) {
                        currentU.value?.let {
                            if (user != null) {
                                commentViewModel.addComment(
                                    recipeId = recipeId,
                                    userId = it.uid,
                                    userName = user.name,
                                    userProfileImage = user.profileImageUrl,
                                    content = commentText
                                )
                            }
                        }
                        commentText = ""
                        Log.d(TAG, "Added comment")
                    }
                }
            )
        }
    }
}

@Composable
fun RecipeDetailContent(
    recipe: Recipe,
    recipeBitmap: Bitmap?,
    navController: NavController,
    modifier: Modifier = Modifier,
    onCommentClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onRateClick: () -> Unit,
    userRating: com.example.baseproject3_foodrecipe.model.Rating?,
    comments: List<com.example.baseproject3_foodrecipe.model.Comment>,
    showComments: Boolean,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onAddComment: () -> Unit
) {
    val commentViewModel: CommentViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Recipe Image
        Box {
            if (recipeBitmap != null) {
                // Display local image
                Image(
                    bitmap = recipeBitmap.asImageBitmap(),
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Display default image
                Image(
                    painter = painterResource(id = R.drawable.italian_pasta),
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Time and cuisine badges
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "${recipe.totalTime} min",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Text(
                        text = recipe.cuisine,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Recipe Title and Author
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onCommentClick) {
                    Icon(
                        Icons.Default.Comment,
                        contentDescription = "Comment",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Comment",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { onAuthorClick(recipe.authorId) }
            ) {
                // Author image
                Image(
                    painter = painterResource(id = R.drawable.chef_avatar), // Replace with actual image
                    contentDescription = "Author",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "by ${recipe.authorName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Recipe Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Rating
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(onClick = onRateClick)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = md_extended_rating,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "${recipe.rating} (${recipe.ratingCount})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (userRating != null) {
                        Text(
                            text = "Your rating: ${userRating.value}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Calories
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "${recipe.calories} kcal",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Time
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "${recipe.totalTime} mins",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Ingredients
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            recipe.ingredients.forEachIndexed { index, ingredient ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ingredient,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Steps
            Text(
                text = "Steps",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            recipe.instructions.forEachIndexed { index, instruction ->
                Row(
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 16.dp, top = 4.dp)
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier
                                .size(32.dp)
                                .wrapContentSize(Alignment.Center),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        text = instruction,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // YouTube Video Section
            recipe.youtubeVideoId?.let { videoId ->
                if (videoId.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Video Tutorial",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate("youtube_player/$videoId") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Watch Video Tutorial")
                        }
                    }
                }
            }

            // Comments Section
            if (showComments) {
                Divider(modifier = Modifier.padding(vertical = 16.dp))

                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Add comment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = onCommentTextChange,
                        placeholder = { Text("Add a comment...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = onAddComment,
                        enabled = commentText.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (commentText.isBlank()) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comments list
                if (comments.isEmpty()) {
                    Text(
                        text = "No comments yet. Be the first to comment!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    comments.forEach { comment ->
                        CommentItem(comment = comment)
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: com.example.baseproject3_foodrecipe.model.Comment) {
    val context = LocalContext.current
    var commentUserBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load user image in a coroutine
    LaunchedEffect(comment.userProfileImage) {
        if (comment.userProfileImage.isNotEmpty()) {
            try {
                commentUserBitmap = LocalImageStorage.loadImage(context, comment.userProfileImage)
            } catch (e: Exception) {
                // Handle error loading image
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            if (commentUserBitmap != null) {
                Image(
                    bitmap = commentUserBitmap!!.asImageBitmap(),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.chef_avatar),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = formatTimestamp(comment.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 48.dp, end = 16.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.padding(start = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* Like comment */ },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = "Like",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
            }

            Text(
                text = "${comment.likes}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Divider(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(start = 48.dp)
        )
    }
}

@Composable
fun RatingDialog(
    currentRating: Double,
    onDismiss: () -> Unit,
    onRateRecipe: (Double) -> Unit
) {
    var rating by remember { mutableStateOf(currentRating) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this recipe") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tap to rate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(5) { index ->
                        val starRating = index + 1
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $starRating",
                            tint = if (starRating <= rating) md_extended_rating else Color.Gray,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { rating = starRating.toDouble() }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = rating.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onRateRecipe(rating) }) {
                Text("Rate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return format.format(date)
}