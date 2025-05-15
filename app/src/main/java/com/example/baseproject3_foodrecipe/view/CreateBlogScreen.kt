package com.example.baseproject3_foodrecipe.view

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import com.example.baseproject3_foodrecipe.viewmodel.BlogViewModel
import com.example.baseproject3_foodrecipe.viewmodel.ImageUploadViewModel
import com.example.baseproject3_foodrecipe.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "CreateBlogScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBlogScreen(
    navController: NavController,
    blogViewModel: BlogViewModel = viewModel(),
    imageUploadViewModel: ImageUploadViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val isLoading by blogViewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Get current user from Firebase Auth
    val auth = FirebaseAuth.getInstance()
    val firebaseUser = auth.currentUser

    // Get user data if available
    val userData by userViewModel.currentUser.collectAsState()

    // Load user data if needed
    LaunchedEffect(firebaseUser) {
        if (firebaseUser != null && userData == null) {
            userViewModel.getUserById(firebaseUser.uid)
        }
    }

    // Use user data if available, fallback to Firebase user info
    var currentUserId by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("") }

    // Update user info when user data changes
    LaunchedEffect(userData, firebaseUser) {
        currentUserId = userData?.id ?: firebaseUser?.uid ?: ""
        // First try to get the display name from Firestore user object
        currentUserName = userData?.name ?:
                // Then try to get it from Firebase Auth
                firebaseUser?.displayName ?:
                // If both fail, check email and take the part before @
                firebaseUser?.email?.substringBefore('@') ?:
                // Last resort
                "Anonymous"

        Log.d(TAG, "User ID: $currentUserId, User Name: $currentUserName")
    }

    // Image upload state
    val isUploading by imageUploadViewModel.isLoading.collectAsState()
    val uploadSuccess by imageUploadViewModel.isSuccess.collectAsState()
    val uploadError by imageUploadViewModel.error.collectAsState()
    val imagePath by imageUploadViewModel.imagePath.collectAsState()

    // Blog form state
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var readTime by remember { mutableStateOf("5") }
    var selectedCategory by remember { mutableStateOf("") }
    var isFeatured by remember { mutableStateOf(false) }

    // Local image state
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var localBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Operation success state
    val operationSuccess by blogViewModel.operationSuccess.collectAsState()
    val errorMessage by blogViewModel.errorMessage.collectAsState()

    // Reset upload state when navigating to this screen
    LaunchedEffect(Unit) {
        imageUploadViewModel.reset()
    }

    // Update local bitmap when image path changes
    LaunchedEffect(imagePath) {
        if (imagePath.isNotEmpty()) {
            Log.d(TAG, "Image path updated: $imagePath")
            try {
                localBitmap = LocalImageStorage.loadImage(context, imagePath)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image: ${e.message}", e)
            }
        }
    }

    // Show snackbar for upload status
    LaunchedEffect(uploadSuccess, uploadError) {
        if (uploadSuccess) {
            Log.d(TAG, "Image upload success")
            snackbarHostState.showSnackbar("Image uploaded successfully")
            imageUploadViewModel.reset()
        } else if (uploadError != null) {
            Log.e(TAG, "Image upload error: $uploadError")
            snackbarHostState.showSnackbar("Error: $uploadError")
            imageUploadViewModel.reset()
        }
    }

    // Show error message if any
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Log.e(TAG, "Blog error: $errorMessage")
            snackbarHostState.showSnackbar("Error: $errorMessage")
            blogViewModel.clearError()
        }
    }

    // Navigate back on successful blog creation
    LaunchedEffect(operationSuccess) {
        if (operationSuccess) {
            Log.d(TAG, "Blog creation success")
            snackbarHostState.showSnackbar("Blog post created successfully")
            blogViewModel.resetOperationSuccess()
            navController.popBackStack()
        }
    }

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Log.d(TAG, "Image selected: $it")
            coroutineScope.launch {
                try {
                    // Save image directly to get path
                    val path = LocalImageStorage.saveImage(context, it, LocalImageStorage.BLOG)
                    Log.d(TAG, "Image saved to: $path")
                    imageUploadViewModel.setImagePath(path)
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving image: ${e.message}", e)
                    snackbarHostState.showSnackbar("Error saving image: ${e.message}")
                }
            }
        }
    }

    // Category options
    val categoryOptions = listOf("Healthy", "Quick Meals", "Desserts", "Vegetarian", "Tips", "Baking", "Breakfast", "Dinner")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Blog Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Photo Upload
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        // Show loading indicator
                        CircularProgressIndicator()
                    } else if (localBitmap != null) {
                        // Show local bitmap
                        Image(
                            bitmap = localBitmap!!.asImageBitmap(),
                            contentDescription = "Blog Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (imageUri != null) {
                        // Show selected image
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Blog Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Show placeholder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Add Photo",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Add Blog Cover Image",
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Blog Title
                Text(
                    text = "Blog Title",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Enter blog title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Summary
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    placeholder = { Text("Brief summary of your blog post") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    maxLines = 3
                )

                // Content
                Text(
                    text = "Content",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Write your blog post content here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    maxLines = 20
                )

                // Read Time
                Text(
                    text = "Read Time (minutes)",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = readTime,
                    onValueChange = { readTime = it },
                    placeholder = { Text("5") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Text("min") }
                )

                // Category
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                // Replace FlowRow with LazyRow
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categoryOptions) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            leadingIcon = if (selectedCategory == category) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }

                // Featured Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Featured Post",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Create Blog Button
                Button(
                    onClick = {
                        Log.d(TAG, "Publish button clicked")

                        if (title.isBlank()) {
                            Log.d(TAG, "Title is blank")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Blog title cannot be empty")
                            }
                            return@Button
                        }

                        if (content.isBlank()) {
                            Log.d(TAG, "Content is blank")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Blog content cannot be empty")
                            }
                            return@Button
                        }

                        if (selectedCategory.isBlank()) {
                            Log.d(TAG, "Category is blank")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please select a category")
                            }
                            return@Button
                        }

                        Log.d(TAG, "All validation passed, creating blog post")
                        Log.d(TAG, "User ID: $currentUserId, User Name: $currentUserName")
                        Log.d(TAG, "Title: $title, Category: $selectedCategory")
                        Log.d(TAG, "Image Path: $imagePath")

                        coroutineScope.launch {
                            try {
                                // Create the blog post with the image path
                                Log.d(TAG, "Calling blogViewModel.createBlogPost")
                                blogViewModel.createBlogPost(
                                    title = title,
                                    content = content,
                                    summary = summary,
                                    imageUrl = imagePath, // Use local path
                                    authorId = currentUserId,
                                    authorName = currentUserName,
                                    category = selectedCategory,
                                    readTime = readTime.toIntOrNull() ?: 5,
                                    featured = isFeatured
                                )
                                Log.d(TAG, "blogViewModel.createBlogPost called successfully")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error creating blog post: ${e.message}", e)
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Publish Blog Post")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
