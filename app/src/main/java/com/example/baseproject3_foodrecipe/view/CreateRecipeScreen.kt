package com.example.baseproject3_foodrecipe.view

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import coil.compose.rememberAsyncImagePainter
import com.example.baseproject3_foodrecipe.viewmodel.ImageUploadViewModel

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

    // Image upload state
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val uploadState by imageUploadViewModel.uploadState.collectAsState()

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
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
                },
                actions = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                // First upload the image if selected
                                var imageUrl = ""
                                if (imageUri != null) {
                                    imageUploadViewModel.uploadImage(imageUri!!, "recipes/${userId}_${System.currentTimeMillis()}")
                                    // Wait for upload to complete
                                    if (uploadState.isSuccess) {
                                        imageUrl = uploadState.downloadUrl ?: ""
                                    }
                                }

                                // Then create the recipe
                                recipeViewModel.createRecipe(
                                    name = recipeName,
                                    description = description,
                                    imageUrl = imageUrl,
                                    authorId = userId,
                                    authorName = userName,
                                    prepTime = prepTime.toIntOrNull() ?: 0,
                                    cookTime = cookTime.toIntOrNull() ?: 0,
                                    servings = servings.toIntOrNull() ?: 0,
                                    difficulty = difficulty,
                                    ingredients = ingredients.filter { it.isNotBlank() },
                                    instructions = instructions.filter { it.isNotBlank() },
                                    categories = selectedCategories.toList(),
                                    calories = calories.toIntOrNull() ?: 0
                                )

                                // Navigate back
                                navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Save")
                    }
                }
            )
        }
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
                    if (imageUri != null) {
                        // Display selected image
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Recipe Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Display placeholder
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

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

                Spacer(modifier = Modifier.height(16.dp))

                // Create Recipe Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            // First upload the image if selected
                            var imageUrl = ""
                            if (imageUri != null) {
                                imageUploadViewModel.uploadImage(imageUri!!, "recipes/${userId}_${System.currentTimeMillis()}")
                                // Wait for upload to complete
                                if (uploadState.isSuccess) {
                                    imageUrl = uploadState.downloadUrl ?: ""
                                }
                            }

                            recipeViewModel.createRecipe(
                                name = recipeName,
                                description = description,
                                imageUrl = imageUrl,
                                authorId = userId,
                                authorName = userName,
                                prepTime = prepTime.toIntOrNull() ?: 0,
                                cookTime = cookTime.toIntOrNull() ?: 0,
                                servings = servings.toIntOrNull() ?: 0,
                                difficulty = difficulty,
                                ingredients = ingredients.filter { it.isNotBlank() },
                                instructions = instructions.filter { it.isNotBlank() },
                                categories = selectedCategories.toList(),
                                calories = calories.toIntOrNull() ?: 0
                            )

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

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalGapPx = 0
        val verticalGapPx = 0

        val rows = mutableListOf<MeasuredRow>()
        var rowConstraints = constraints
        var rowPlaceables = mutableListOf<Placeable>()
        var rowWidth = 0
        var rowHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(rowConstraints)

            if (rowWidth + placeable.width > constraints.maxWidth) {
                rows.add(
                    MeasuredRow(
                        placeables = rowPlaceables,
                        width = rowWidth - horizontalGapPx,
                        height = rowHeight
                    )
                )

                rowPlaceables = mutableListOf()
                rowWidth = 0
                rowHeight = 0
            }

            rowPlaceables.add(placeable)
            rowWidth += placeable.width + horizontalGapPx
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        if (rowPlaceables.isNotEmpty()) {
            rows.add(
                MeasuredRow(
                    placeables = rowPlaceables,
                    width = rowWidth - horizontalGapPx,
                    height = rowHeight
                )
            )
        }

        val width = rows.maxOfOrNull { row -> row.width } ?: 0
        val height = rows.sumBy { row -> row.height } + (rows.size - 1) * verticalGapPx

        layout(width, height) {
            var y = 0

            rows.forEach { row ->
                var x = when (horizontalArrangement) {
                    Arrangement.Start -> 0
                    Arrangement.Center -> (width - row.width) / 2
                    Arrangement.End -> width - row.width
                    Arrangement.SpaceBetween -> 0
                    Arrangement.SpaceAround -> 0
                    Arrangement.SpaceEvenly -> 0
                    else -> 0
                }

                row.placeables.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + horizontalGapPx
                }

                y += row.height + verticalGapPx
            }
        }
    }
}

data class MeasuredRow(
    val placeables: List<Placeable>,
    val width: Int,
    val height: Int
)
