package com.example.baseproject3_foodrecipe.view

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.model.User
import com.example.baseproject3_foodrecipe.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    searchViewModel: SearchViewModel = viewModel()
) {
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    searchViewModel.search(it)
                },
                placeholder = { Text("Search recipes, users...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchViewModel.clearSearch()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (searchQuery.isNotEmpty() && searchResults.recipes.isEmpty() && searchResults.users.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No results found for \"$searchQuery\"")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Recipes section
                    if (searchResults.recipes.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recipes",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        items(searchResults.recipes) { recipe ->
                            RecipeSearchItem(
                                recipe = recipe,
                                onClick = { navController.navigate("recipe_detail/${recipe.id}") }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }

                    // Users section
                    if (searchResults.users.isNotEmpty()) {
                        item {
                            Text(
                                text = "Users",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        items(searchResults.users) { user ->
                            UserSearchItem(
                                user = user,
                                onClick = { navController.navigate("profile/${user.id}") }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeSearchItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Recipe image
        Image(
            painter = painterResource(id = R.drawable.italian_pasta), // Use actual image in production
            contentDescription = recipe.name,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Recipe details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "by ${recipe.authorName}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFC107)
                )

                Text(
                    text = "${recipe.rating} (${recipe.ratingCount})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )

                Text(
                    text = "${recipe.totalTime} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "View",
            tint = Color.Gray
        )
    }
}

@Composable
fun UserSearchItem(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User avatar
        Image(
            painter = painterResource(id = R.drawable.chef_avatar), // Use actual image in production
            contentDescription = user.name,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // User details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            if (user.chef) {
                Text(
                    text = user.chefTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "View",
            tint = Color.Gray
        )
    }
}
