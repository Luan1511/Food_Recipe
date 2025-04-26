package com.example.baseproject3_foodrecipe.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.baseproject3_foodrecipe.ui.theme.FoodRecipeTheme
import com.example.baseproject3_foodrecipe.viewmodel.AuthViewModel
import com.example.baseproject3_foodrecipe.viewmodel.BlogViewModel
import com.example.baseproject3_foodrecipe.viewmodel.ImageUploadViewModel
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import com.example.baseproject3_foodrecipe.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    // Initialize ViewModels at the activity level
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var imageUploadViewModel: ImageUploadViewModel
    private lateinit var blogViewModel: BlogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModels
        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        imageUploadViewModel = ViewModelProvider(this)[ImageUploadViewModel::class.java]
        blogViewModel = ViewModelProvider(this)[BlogViewModel::class.java]

        setContent {
            FoodRecipeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = if (authViewModel.currentUser.value != null) "home" else "login"
                    ) {
                        // Auth screens
                        composable("login") {
                            LoginScreen(
                                navController = navController,
                                authViewModel = authViewModel
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                navController = navController,
                                authViewModel = authViewModel
                            )
                        }

                        composable("forgot_password") {
                            ForgotPasswordScreen(
                                navController = navController,
                                authViewModel = authViewModel
                            )
                        }

                        // Main app screens
                        composable("home") {
                            HomeScreen(
                                navController = navController,
                                recipeViewModel = recipeViewModel,
                                userViewModel = userViewModel
                            )
                        }

                        composable(
                            "recipe_detail/{recipeId}",
                            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                            RecipeDetailScreen(
                                navController = navController,
                                recipeId = recipeId,
                                recipeViewModel = recipeViewModel,
                                userViewModel = userViewModel
                            )
                        }

                        composable(
                            "create_recipe/{userId}/{userName}",
                            arguments = listOf(
                                navArgument("userId") { type = NavType.StringType },
                                navArgument("userName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            val userName = backStackEntry.arguments?.getString("userName") ?: ""
                            CreateRecipeScreen(
                                navController = navController,
                                userId = userId,
                                userName = userName,
                                recipeViewModel = recipeViewModel,
                                imageUploadViewModel = imageUploadViewModel
                            )
                        }

                        composable(
                            "profile/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            ProfileScreen(
                                navController = navController,
                                userId = userId,
                                userViewModel = userViewModel,
                                recipeViewModel = recipeViewModel
                            )
                        }

                        composable(
                            "video_player/{recipeId}",
                            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                            VideoPlayerScreen(
                                navController = navController,
                                recipeId = recipeId,
                                recipeViewModel = recipeViewModel
                            )
                        }

                        composable(
                            "category/{categoryName}",
                            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                            CategoryScreen(
                                navController = navController,
                                categoryName = categoryName,
                                recipeViewModel = recipeViewModel
                            )
                        }

                        // New screens
                        composable("saved_recipes") {
                            SavedRecipesScreen(
                                navController = navController,
                                userViewModel = userViewModel,
                                recipeViewModel = recipeViewModel
                            )
                        }

                        composable("my_recipes") {
                            MyRecipesScreen(
                                navController = navController,
                                userViewModel = userViewModel,
                                recipeViewModel = recipeViewModel
                            )
                        }

                        composable("blog") {
                            BlogScreen(
                                navController = navController,
                                blogViewModel = blogViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
