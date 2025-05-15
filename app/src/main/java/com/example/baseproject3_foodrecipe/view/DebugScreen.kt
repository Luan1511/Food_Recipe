package com.example.baseproject3_foodrecipe.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel

@Composable
fun DebugScreen(recipeViewModel: RecipeViewModel) {
    val recipes by recipeViewModel.recipes.collectAsState()
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val errorMessage by recipeViewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Debug Screen",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { recipeViewModel.loadAllRecipes() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reload Recipes")
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        errorMessage?.let {
            Text(
                text = "Error: $it",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Text(
            text = "Recipe Count: ${recipes.size}",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn {
            items(recipes) { recipe ->
                RecipeDebugItem(recipe)
            }
        }
    }
}

@Composable
fun RecipeDebugItem(recipe: Recipe) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ID: ${recipe.id}",
                fontWeight = FontWeight.Bold
            )
            Text(text = "Name: ${recipe.name}")
            Text(text = "Author: ${recipe.authorName} (${recipe.authorId})")
            Text(text = "Description: ${recipe.description}")
            Text(text = "Categories: ${recipe.categories.joinToString()}")
            Text(text = "Prep Time: ${recipe.prepTime}, Cook Time: ${recipe.cookTime}")
            Text(text = "Calories: ${recipe.calories}")
            Text(text = "Featured: ${recipe.featured}")
            Text(text = "Created At: ${recipe.createdAt}")
        }
    }
}
