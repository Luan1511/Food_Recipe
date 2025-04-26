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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.ui.theme.*

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    featuredRecipes: List<Recipe>,
    popularRecipes: List<Recipe>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Featured Videos Section
        Text(
            text = "Featured Videos",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (featuredRecipes.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Take first two recipes for featured videos
                val videoRecipes = featuredRecipes.take(2)

                videoRecipes.forEach { recipe ->
                    VideoCard(
                        title = recipe.name,
                        chef = recipe.authorName,
                        imageRes = R.drawable.italian_pasta, // Use placeholder for now
                        modifier = Modifier.weight(1f),
                        onClick = {
                            navController.navigate("video_player/${recipe.id}")
                        }
                    )
                }

                // If we don't have enough recipes, add placeholders
                if (videoRecipes.size < 2) {
                    repeat(2 - videoRecipes.size) {
                        VideoCard(
                            title = "Coming Soon",
                            chef = "Chef",
                            imageRes = R.drawable.italian_pasta,
                            modifier = Modifier.weight(1f),
                            onClick = { }
                        )
                    }
                }
            }
        } else {
            // Placeholder if no recipes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VideoCard(
                    title = "Italian Pasta",
                    chef = "Chef Mario",
                    imageRes = R.drawable.italian_pasta,
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )

                VideoCard(
                    title = "Sushi Making",
                    chef = "Chef Yuki",
                    imageRes = R.drawable.italian_pasta,
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Popular Recipes Section
        Text(
            text = "Popular Recipes",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (popularRecipes.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Take first two popular recipes
                val topRecipes = popularRecipes.take(2)

                topRecipes.forEach { recipe ->
                    RecipeCard(
                        title = recipe.name,
                        cookingTime = "${recipe.totalTime} min",
                        difficulty = recipe.difficulty,
                        imageRes = R.drawable.italian_pasta, // Use placeholder for now
                        modifier = Modifier.weight(1f),
                        onClick = {
                            navController.navigate("recipe_detail/${recipe.id}")
                        }
                    )
                }

                // If we don't have enough recipes, add placeholders
                if (topRecipes.size < 2) {
                    repeat(2 - topRecipes.size) {
                        RecipeCard(
                            title = "Coming Soon",
                            cookingTime = "30 min",
                            difficulty = "Easy",
                            imageRes = R.drawable.italian_pasta,
                            modifier = Modifier.weight(1f),
                            onClick = { }
                        )
                    }
                }
            }
        } else {
            // Placeholder if no recipes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RecipeCard(
                    title = "Butter Chicken",
                    cookingTime = "30 min",
                    difficulty = "Medium",
                    imageRes = R.drawable.italian_pasta,
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )

                RecipeCard(
                    title = "Chocolate Cake",
                    cookingTime = "45 min",
                    difficulty = "Easy",
                    imageRes = R.drawable.italian_pasta,
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Categories Section
        Text(
            text = "Categories",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CategoryItem(
                title = "Asian",
                iconRes = R.drawable.ic_asian,
                backgroundColor = Color(0xFFFFF3E0),
                iconTint = md_food_sushi,
                onClick = { navController.navigate("category/Asian") }
            )

            CategoryItem(
                title = "Italian",
                iconRes = R.drawable.ic_italian,
                backgroundColor = Color(0xFFFFEBEE),
                iconTint = Color(0xFFE53935),
                onClick = { navController.navigate("category/Italian") }
            )

            CategoryItem(
                title = "Vegan",
                iconRes = R.drawable.ic_vegan,
                backgroundColor = Color(0xFFE8F5E9),
                iconTint = md_food_veggie,
                onClick = { navController.navigate("category/Vegan") }
            )

            CategoryItem(
                title = "Desserts",
                iconRes = R.drawable.ic_dessert,
                backgroundColor = Color(0xFFF3E5F5),
                iconTint = md_food_dessert,
                onClick = { navController.navigate("category/Dessert") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Blog Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Food Blog",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )

            TextButton(
                onClick = { navController.navigate("blog") }
            ) {
                Text("View All")
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "View All",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Blog preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("blog") },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.italian_pasta),
                    contentDescription = "Blog Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Latest Food Trends",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Discover the latest culinary trends and get inspired for your next cooking adventure.",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Chef Michael",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "5 min read",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun VideoCard(
    title: String,
    chef: String,
    imageRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Offline indicator instead of LIVE
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF4CAF50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .align(Alignment.TopStart)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "PLAY",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(Color(0x99000000))
                    .padding(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = chef,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    title: String,
    iconRes: Int,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = iconTint
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}
