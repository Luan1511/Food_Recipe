package com.example.baseproject3_foodrecipe.view

import android.annotation.SuppressLint
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
import coil.compose.AsyncImage
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.model.AuthRepository
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.model.YouTubeVideo
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import com.example.baseproject3_foodrecipe.viewmodel.AuthViewModel
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import com.example.baseproject3_foodrecipe.viewmodel.YouTubeViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    featuredRecipes: List<Recipe>,
    popularRecipes: List<Recipe>,
) {
    val scrollState = rememberScrollState()
    val youtubeViewModel: YouTubeViewModel = viewModel()
    val trendingVideos by youtubeViewModel.trendingVideos.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp) // Add padding for bottom navigation
    ) {
        // Welcome Section
        WelcomeSection()

        // Featured Recipes Section
        if (featuredRecipes.isNotEmpty()) {
            SectionTitle("Featured Recipes")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(featuredRecipes) { recipe ->
                    RecipeCardComponent(
                        recipe = recipe,
                        onClick = { navController.navigate("recipe_detail/${recipe.id}") },
                        modifier = Modifier.width(280.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Popular Recipes Section
        if (popularRecipes.isNotEmpty()) {
            SectionTitle("Popular Recipes")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(popularRecipes) { recipe ->
                    RecipeCardComponent(
                        recipe = recipe,
                        onClick = { navController.navigate("recipe_detail/${recipe.id}") },
                        modifier = Modifier.width(280.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // YouTube Cooking Videos Section
        SectionTitle("Cooking Videos")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(trendingVideos) { video ->
                VideoCard(
                    video = video,
                    onClick = { navController.navigate("youtube_player/${video.id}") },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Categories Section
//        SectionTitle("Categories")
//        CategoriesSection(navController)

        Spacer(modifier = Modifier.height(24.dp))

        // Video Recipes


        Spacer(modifier = Modifier.height(80.dp)) // Bottom spacing for FAB
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "What would you like to cook today?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Discover recipes, videos, and cooking tips",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
//        OutlinedTextField(
//            value = "",
//            onValueChange = { },
//            modifier = Modifier.fillMaxWidth(),
//            placeholder = { Text("Search recipes...") },
//            leadingIcon = {
//                Icon(
//                    Icons.Default.Search,
//                    contentDescription = "Search"
//                )
//            },
//            shape = RoundedCornerShape(12.dp),
//            colors = TextFieldDefaults.outlinedTextFieldColors(
//                focusedBorderColor = MaterialTheme.colorScheme.primary,
//                unfocusedBorderColor = Color.LightGray
//            )
//        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun CategoriesSection(navController: NavController) {
    val categories = listOf(
        Triple("Asian", "ic_asian", "asian"),
        Triple("Italian", "ic_italian", "italian"),
        Triple("Vegan", "ic_vegan", "vegan"),
        Triple("Dessert", "ic_dessert", "dessert")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        categories.forEach { (name, icon, route) ->
            CategoryItem(
                name = name,
                iconName = icon,
                onClick = { navController.navigate("category/$route") }
            )
        }
    }
}

@Composable
fun CategoryItem(
    name: String,
    iconName: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (iconName) {
                "ic_asian" -> R.drawable.ic_asian
                "ic_italian" -> R.drawable.ic_italian
                "ic_vegan" -> R.drawable.ic_vegan
                "ic_dessert" -> R.drawable.ic_dessert
                else -> R.drawable.ic_launcher_foreground // Fallback icon
            }
            Icon(
                painter = painterResource(id = icon),
                contentDescription = name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun RecipeCardComponent(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var recipeBitmap by remember { mutableStateOf<Any?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val recipeviewModel: RecipeViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    LaunchedEffect(recipe.imageUrl) {
        if (recipe.imageUrl.isNotEmpty()) {
            coroutineScope.launch {
                if (LocalImageStorage.fileExists(context, recipe.imageUrl)) {
                    recipeBitmap = LocalImageStorage.loadImage(context, recipe.imageUrl)
                }
            }
        }
    }

    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Recipe image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                if (recipeBitmap != null) {
                    AsyncImage(
                        model = recipeBitmap,
                        contentDescription = recipe.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recipe.name.take(1),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (authViewModel.isAdmin.value){
                Button(
                    onClick = { recipeviewModel.deleteRecipe(recipe.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
