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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.model.BlogPost
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import com.example.baseproject3_foodrecipe.viewmodel.AuthViewModel
import com.example.baseproject3_foodrecipe.viewmodel.BlogViewModel
import com.example.baseproject3_foodrecipe.viewmodel.CommentViewModel
import com.example.baseproject3_foodrecipe.viewmodel.RatingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogDetailScreen(
    navController: NavController,
    blogId: String,
    blogViewModel: BlogViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    commentViewModel: CommentViewModel = viewModel(),
    ratingViewModel: RatingViewModel = viewModel()
) {
    val currentBlog by blogViewModel.currentBlog.collectAsState()
    val isLoading by blogViewModel.isLoading.collectAsState()
    val currentUser = authViewModel.currentUser.collectAsState().value
    val comments by commentViewModel.comments.collectAsState()
    val userRating by ratingViewModel.userRating.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    var isLiked by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showCommentSection by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // For local image loading
    var blogBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load blog data
    LaunchedEffect(blogId) {
        blogViewModel.getBlogById(blogId)
        commentViewModel.loadCommentsByBlogId(blogId)
        currentUser?.uid?.let { userId ->
            ratingViewModel.getUserRatingForBlog(userId, blogId)
        }
    }

    // Check if user has liked the blog
    LaunchedEffect(currentBlog, currentUser) {
        if (currentBlog != null && currentUser != null) {
            isLiked = currentBlog!!.likedBy.contains(currentUser.uid)
        }
    }

    // Load local image if path exists
    LaunchedEffect(currentBlog) {
        currentBlog?.let { blog ->
            if (blog.imageUrl.isNotEmpty()) {
                try {
                    blogBitmap = LocalImageStorage.loadImage(context, blog.imageUrl)
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
                currentUser?.uid?.let { userId ->
                    ratingViewModel.addRating(
                        blogId = blogId,
                        userId = userId,
                        userName = currentUser.displayName ?: "User",
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
                title = { Text("Blog Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentUser != null) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                if (isLiked) {
                                    blogViewModel.unlikeBlogPost(blogId, currentUser.uid)
                                    isLiked = false
                                    snackbarHostState.showSnackbar("Blog unliked")
                                } else {
                                    blogViewModel.likeBlogPost(blogId, currentUser.uid)
                                    isLiked = true
                                    snackbarHostState.showSnackbar("Blog liked")
                                }
                            }
                        }) {
                            Icon(
                                if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) Color.Red else Color.Gray
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLoading || currentBlog == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Blog Image
                item {
                    if (blogBitmap != null) {
                        Image(
                            bitmap = blogBitmap!!.asImageBitmap(),
                            contentDescription = "Blog Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.italian_pasta),
                            contentDescription = "Blog Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Blog Content
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Title
                        Text(
                            text = currentBlog!!.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Author and Date
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "By ${currentBlog!!.authorName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    navController.navigate("profile/${currentBlog!!.authorId}")
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = formatBlogTimestamp(currentBlog!!.publishDate),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category and Read Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = currentBlog!!.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "${currentBlog!!.readTime} min read",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Likes
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "${currentBlog!!.likes} likes",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Comments
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { showCommentSection = !showCommentSection }
                            ) {
                                Icon(
                                    Icons.Default.Comment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "${currentBlog!!.commentCount} comments",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Rating
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable(onClick = {
                                    if (currentUser != null) {
                                        showRatingDialog = true
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Please login to rate")
                                        }
                                    }
                                })
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(24.dp)
                                )
                                if (userRating != null) {
                                    Text(
                                        text = "Your rating: ${userRating!!.value}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    Text(
                                        text = "Rate this blog",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Summary
                        Text(
                            text = currentBlog!!.summary,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Content
                        Text(
                            text = currentBlog!!.content,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Actions
                        val isAuthor = currentUser?.uid == currentBlog!!.authorId
                        if (isAuthor || isAdmin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (isAuthor) {
                                    OutlinedButton(
                                        onClick = {
                                            // Navigate to edit blog
                                            navController.navigate("edit_blog/${currentBlog!!.id}")
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit")
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            if (isAdmin) {
                                                blogViewModel.adminDeleteBlogPost(currentBlog!!.id)
                                            } else {
                                                currentUser?.let { user ->
                                                    blogViewModel.deleteBlogPost(currentBlog!!.id, user.uid)
                                                }
                                            }
                                            snackbarHostState.showSnackbar("Blog post deleted")
                                            navController.popBackStack()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }

                // Comments Section
                if (showCommentSection) {
                    item {
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Comments",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Add comment
                            if (currentUser != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = commentText,
                                        onValueChange = { commentText = it },
                                        placeholder = { Text("Add a comment...") },
                                        modifier = Modifier.weight(1f),
                                        maxLines = 3
                                    )

                                    IconButton(
                                        onClick = {
                                            if (commentText.isNotBlank()) {
                                                commentViewModel.addComment(
                                                    blogId = blogId,
                                                    userId = currentUser.uid,
                                                    userName = currentUser.displayName ?: "User",
                                                    userProfileImage = currentUser.photoUrl?.toString() ?: "",
                                                    content = commentText
                                                )
                                                commentText = ""
                                            }
                                        },
                                        enabled = commentText.isNotBlank()
                                    ) {
                                        Icon(
                                            Icons.Default.Send,
                                            contentDescription = "Send",
                                            tint = if (commentText.isBlank()) Color.Gray else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { navController.navigate("login") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Login to comment")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Comments list
                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No comments yet. Be the first to comment!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            CommentItem(
                                comment = comment,
                                currentUserId = currentUser?.uid,
                                onLike = { commentId ->
                                    currentUser?.uid?.let { userId ->
                                        commentViewModel.likeComment(commentId, userId)
                                    }
                                },
                                onDelete = { commentId ->
                                    currentUser?.uid?.let { userId ->
                                        commentViewModel.deleteComment(commentId, userId)
                                    }
                                },
                                isAdmin = isAdmin
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: com.example.baseproject3_foodrecipe.model.Comment,
    currentUserId: String?,
    onLike: (String) -> Unit,
    onDelete: (String) -> Unit,
    isAdmin: Boolean
) {
    val context = LocalContext.current
    var commentUserBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val isLiked = comment.likedBy.contains(currentUserId)
    val isOwner = currentUserId == comment.userId

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
            .padding(horizontal = 16.dp, vertical = 8.dp)
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

            // Delete button for owner or admin
            if (isOwner || isAdmin) {
                IconButton(
                    onClick = { onDelete(comment.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
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
                onClick = { onLike(comment.id) },
                modifier = Modifier.size(36.dp),
                enabled = currentUserId != null
            ) {
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = "Like",
                    modifier = Modifier.size(16.dp),
                    tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.Gray
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
