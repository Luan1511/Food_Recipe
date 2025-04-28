package com.example.baseproject3_foodrecipe.view

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.ui.theme.md_extended_rating
import com.example.baseproject3_foodrecipe.viewmodel.CommentViewModel
import com.example.baseproject3_foodrecipe.viewmodel.RatingViewModel
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import com.example.baseproject3_foodrecipe.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeId: String,
    recipeViewModel: RecipeViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    commentViewModel: CommentViewModel = viewModel(),
    ratingViewModel: RatingViewModel = viewModel()
) {
    val currentRecipe by recipeViewModel.currentRecipe.collectAsState()
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    val comments by commentViewModel.comments.collectAsState()
    val userRating by ratingViewModel.userRating.collectAsState()

    var isBookmarked by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showCommentSection by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load recipe data and check if it's bookmarked
    LaunchedEffect(recipeId, currentUser) {
        recipeViewModel.getRecipeById(recipeId)
        currentUser?.id?.let { userId ->
            isBookmarked = recipeViewModel.isRecipeSaved(recipeId, userId)
            ratingViewModel.getUserRating(recipeId, userId)
        }
        commentViewModel.getCommentsByRecipe(recipeId)
    }

    // Rating dialog
    if (showRatingDialog) {
        RatingDialog(
            currentRating = userRating ?: 0.0,
            onDismiss = { showRatingDialog = false },
            onRateRecipe = { rating ->
                currentUser?.id?.let { userId ->
                    ratingViewModel.rateRecipe(recipeId, userId, rating)
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
                        currentUser?.id?.let { userId ->
                            coroutineScope.launch {
                                if (isBookmarked) {
                                    recipeViewModel.unbookmarkRecipe(recipeId, userId)
                                    isBookmarked = false
                                    snackbarHostState.showSnackbar("Recipe removed from saved")
                                } else {
                                    recipeViewModel.bookmarkRecipe(recipeId, userId)
                                    isBookmarked = true
                                    snackbarHostState.showSnackbar("Recipe saved")
                                }
                            }
                        }
                    }) {
                        Icon(
                            if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark"
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
                    if (commentText.isNotBlank() && currentUser != null) {
                        commentViewModel.addComment(
                            recipeId = recipeId,
                            userId = currentUser!!.id,
                            userName = currentUser!!.name,
                            userImageUrl = currentUser!!.profileImageUrl,
                            content = commentText
                        )
                        commentText = ""
                    }
                }
            )
        }
    }
}

@Composable
fun RecipeDetailContent(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onCommentClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onRateClick: () -> Unit,
    userRating: Double?,
    comments: List<com.example.baseproject3_foodrecipe.model.Comment>,
    showComments: Boolean,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onAddComment: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Recipe Image
        Box {
            if (recipe.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(recipe.imageUrl),
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
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
                            text = "Your rating: $userRating",
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            Image(
                painter = if (comment.userImageUrl.isNotEmpty())
                    rememberAsyncImagePainter(comment.userImageUrl)
                else
                    painterResource(id = R.drawable.chef_avatar),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

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
