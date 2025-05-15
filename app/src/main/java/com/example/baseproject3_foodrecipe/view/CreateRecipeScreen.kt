package com.example.baseproject3_foodrecipe.view

import android.graphics.Bitmap
import android.net.Uri
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
import com.example.baseproject3_foodrecipe.viewmodel.ImageUploadViewModel
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    navController: NavController,
    userId: String,
    userName: String,
    recipeViewModel: RecipeViewModel = viewModel(),
    imageUploadViewModel: ImageUploadViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Image upload state
    val isUploading by imageUploadViewModel.isLoading.collectAsState()
    val uploadSuccess by imageUploadViewModel.isSuccess.collectAsState()
    val uploadError by imageUploadViewModel.error.collectAsState()
    val imagePath by imageUploadViewModel.imagePath.collectAsState()

    // Recipe form state
    var recipeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Easy") }
    var ingredients by remember { mutableStateOf(listOf("")) }
    var instructions by remember { mutableStateOf(listOf("")) }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var youtubeVideoId by remember { mutableStateOf("") }

    // Nutrition values
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var cuisine by remember { mutableStateOf("") }

    // Local image state
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var localBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Reset upload state when navigating to this screen
    LaunchedEffect(Unit) {
        imageUploadViewModel.reset()
    }

    // Update local bitmap when image path changes
    LaunchedEffect(imagePath) {
        if (imagePath.isNotEmpty()) {
            localBitmap = LocalImageStorage.loadImage(context, imagePath)
        }
    }

    // Show snackbar for upload status
    LaunchedEffect(uploadSuccess, uploadError) {
        if (uploadSuccess) {
            snackbarHostState.showSnackbar("Image uploaded successfully")
            imageUploadViewModel.reset()
        } else if (uploadError != null) {
            snackbarHostState.showSnackbar("Error: $uploadError")
            imageUploadViewModel.reset()
        }
    }

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    // Difficulty options
    val difficultyOptions = listOf("Easy", "Medium", "Hard")

    // Category options
    val categoryOptions = listOf("Breakfast", "Lunch", "Dinner", "Dessert", "Vegetarian", "Italian", "Asian", "Mexican")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Recipe") },
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
                            contentDescription = "Recipe Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (imageUri != null) {
                        // Show selected image
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Recipe Image",
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
                                "Add Recipe Photo",
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Recipe Name
                Text(
                    text = "Recipe Name",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = recipeName,
                    onValueChange = { recipeName = it },
                    placeholder = { Text("Enter recipe name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Brief description of your recipe") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                // Prep Time and Cook Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Prep Time",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        OutlinedTextField(
                            value = prepTime,
                            onValueChange = { prepTime = it },
                            placeholder = { Text("20") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = { Text("min") }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cook Time",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        OutlinedTextField(
                            value = cookTime,
                            onValueChange = { cookTime = it },
                            placeholder = { Text("30") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = { Text("min") }
                        )
                    }
                }

                // Servings and Difficulty
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Servings",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        OutlinedTextField(
                            value = servings,
                            onValueChange = { servings = it },
                            placeholder = { Text("4") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = { Text("servings") }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Difficulty",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )

                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = difficulty,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                difficultyOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            difficulty = option
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Calories
                Text(
                    text = "Calories",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    placeholder = { Text("450") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Text("kcal") }
                )

                // Nutritional Information
                Text(
                    text = "Nutritional Information",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Protein",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        OutlinedTextField(
                            value = protein,
                            onValueChange = { protein = it },
                            placeholder = { Text("20") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = { Text("g") }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Carbs",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        OutlinedTextField(
                            value = carbs,
                            onValueChange = { carbs = it },
                            placeholder = { Text("30") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = { Text("g") }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fat",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        OutlinedTextField(
                            value = fat,
                            onValueChange = { fat = it },
                            placeholder = { Text("15") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = { Text("g") }
                        )
                    }
                }

                // Cuisine
                Text(
                    text = "Cuisine",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = cuisine,
                    onValueChange = { cuisine = it },
                    placeholder = { Text("Italian") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Ingredients
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    TextButton(
                        onClick = {
                            ingredients = ingredients + ""
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Text("Add")
                    }
                }

                ingredients.forEachIndexed { index, ingredient ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = ingredient,
                            onValueChange = { newValue ->
                                ingredients = ingredients.toMutableList().apply {
                                    this[index] = newValue
                                }
                            },
                            placeholder = { Text("Any Ingredient") },
                            modifier = Modifier.weight(1f)
                        )

                        if (ingredients.size > 1) {
                            IconButton(
                                onClick = {
                                    ingredients = ingredients.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }

                // Instructions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    TextButton(
                        onClick = {
                            instructions = instructions + ""
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Step")
                        Text("Add Step")
                    }
                }

                instructions.forEachIndexed { index, instruction ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(end = 8.dp, top = 8.dp)
                        ) {
                            Text(
                                text = "${index + 1}",
                                modifier = Modifier
                                    .size(24.dp)
                                    .wrapContentSize(Alignment.Center)
                            )
                        }

                        OutlinedTextField(
                            value = instruction,
                            onValueChange = { newValue ->
                                instructions = instructions.toMutableList().apply {
                                    this[index] = newValue
                                }
                            },
                            placeholder = { Text("Describe this step") },
                            modifier = Modifier.weight(1f),
                            minLines = 2
                        )

                        if (instructions.size > 1) {
                            IconButton(
                                onClick = {
                                    instructions = instructions.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }

                // Categories
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                // Use a simple Row with wrapping for categories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryOptions.forEach { category ->
                        val isSelected = selectedCategories.contains(category)

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategories = if (isSelected) {
                                    selectedCategories - category
                                } else {
                                    selectedCategories + category
                                }
                            },
                            label = { Text(category) },
                            leadingIcon = if (isSelected) {
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

                // YouTube Video ID Field
                OutlinedTextField(
                    value = youtubeVideoId,
                    onValueChange = { youtubeVideoId = it },
                    label = { Text("YouTube Video ID or URL") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                // Helper text for YouTube video
                Text(
                    text = "Enter a YouTube video ID or full URL",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Create Recipe Button
                Button(
                    onClick = {
                        if (recipeName.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Recipe name cannot be empty")
                            }
                            return@Button
                        }

                        coroutineScope.launch {
                            // Extract YouTube video ID if a full URL was provided
                            val extractedVideoId =
                                if (youtubeVideoId.contains("youtube.com") || youtubeVideoId.contains("youtu.be")) {
                                    try {
                                        val youTubeApiService = YouTubeApiService()
                                        youTubeApiService.extractVideoIdFromUrl(youtubeVideoId)
                                    } catch (e: Exception) {
                                        youtubeVideoId
                                    }
                                } else {
                                    youtubeVideoId
                                }

                            if (imageUri != null) {
                                // If we have an image, save it and create recipe
                                recipeViewModel.saveImageAndCreateRecipe(
                                    context = context,
                                    imageUri = imageUri!!,
                                    name = recipeName,
                                    description = description,
                                    ingredients = ingredients.filter { it.isNotBlank() },
                                    instructions = instructions.filter { it.isNotBlank() },
                                    prepTime = prepTime.toIntOrNull() ?: 0,
                                    cookTime = cookTime.toIntOrNull() ?: 0,
                                    servings = servings.toIntOrNull() ?: 0,
                                    difficulty = difficulty,
                                    categories = selectedCategories.toList(),
                                    authorId = userId,
                                    authorName = userName,
                                    calories = calories.toIntOrNull() ?: 0,
//                                    protein = protein.toIntOrNull() ?: 0,
//                                    carbs = carbs.toIntOrNull() ?: 0,
//                                    fat = fat.toIntOrNull() ?: 0,
//                                    cuisine = cuisine,
                                    youtubeVideoId = extractedVideoId
                                )
                            } else {
                                // Create recipe without image
                                recipeViewModel.createRecipe(
                                    name = recipeName,
                                    description = description,
                                    ingredients = ingredients.filter { it.isNotBlank() },
                                    instructions = instructions.filter { it.isNotBlank() },
                                    prepTime = prepTime.toIntOrNull() ?: 0,
                                    cookTime = cookTime.toIntOrNull() ?: 0,
                                    servings = servings.toIntOrNull() ?: 0,
                                    difficulty = difficulty,
                                    categories = selectedCategories.toList(),
                                    authorId = userId,
                                    authorName = userName,
                                    calories = calories.toIntOrNull() ?: 0,
//                                    protein = protein.toIntOrNull() ?: 0,
//                                    carbs = carbs.toIntOrNull() ?: 0,
//                                    fat = fat.toIntOrNull() ?: 0,
//                                    cuisine = cuisine,
                                    youtubeVideoId = extractedVideoId
                                )
                            }

                            // Navigate back
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Create Recipe")
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

class YouTubeApiService {
    fun extractVideoIdFromUrl(url: String): String {
        val videoId = when {
            url.contains("youtube.com") -> {
                val regex = "(?:v=)([^&]+)".toRegex()
                regex.find(url)?.groupValues?.get(1) ?: ""
            }
            url.contains("youtu.be") -> {
                url.substringAfterLast("/")
            }
            else -> url
        }
        return videoId
    }
}
