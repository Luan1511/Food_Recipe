package com.example.baseproject3_foodrecipe.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.baseproject3_foodrecipe.ui.theme.FoodRecipeTheme
import com.example.baseproject3_foodrecipe.view.admins.BlogsManageScreen
import com.example.baseproject3_foodrecipe.view.admins.RecipesManageScreen
import com.example.baseproject3_foodrecipe.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodRecipeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FoodRecipeApp()
                }
            }
        }
        window.statusBarColor = Color.parseColor("#FFFFFF")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.firstOrNull()

            spokenText?.let {
                Log.d("STT", "Văn bản bạn nói: $it")
            }
        }
    }
}

@Composable
fun FoodRecipeApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    Scaffold(
        bottomBar = {
            // Only show bottom navigation on main screens and when user is logged in
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val mainRoutes = listOf("home", "planner", "saved", "videos", "blog")
            val showBottomBar = (mainRoutes.any { route ->
                currentRoute == route || currentRoute?.startsWith("$route/") == true
            } || currentRoute?.startsWith("profile/") == true) && currentUser != null

            if (showBottomBar) {
                BottomNavigation(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(navController = navController, startDestination = "home") {
                // Main screens
                composable("home") {
                    HomeScreen(navController = navController)
                }
                composable("planner") {
                    MealPlannerScreen(
                        navController = navController,
                        mealPlanViewModel = viewModel()
                    )
                }
                composable("saved") {
                    SavedRecipesScreen(
                        navController = navController,
                        recipeViewModel = viewModel()
                    )
                }
                composable("videos") {
                    VideoScreen(
                        navController = navController
                    )
                }

                // Main blog screen
                composable("blog") {
                    BlogListScreen(
                        navController = navController,
                        blogViewModel = viewModel()
                    )
                }

                // Profile screen with userId parameter
                composable(
                    "profile/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    ProfileScreen(
                        navController = navController,
                        userId = userId,
                        userViewModel = viewModel(),
                        authViewModel = authViewModel,
                        blogViewModel = viewModel()
                    )
                }

                // Authentication screens
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

                // Recipe screens
                composable(
                    "recipe_detail/{recipeId}",
                    arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                    RecipeDetailScreen(
                        recipeId = recipeId,
                        navController = navController,
                        recipeViewModel = viewModel(),
                        commentViewModel = viewModel(),
                        ratingViewModel = viewModel()
                    )
                }
                composable("create_recipe") {
                    // Get current user ID from AuthViewModel
                    val userId = currentUser?.uid ?: ""
                    val userName = currentUser?.displayName ?: "Anonymous"

                    CreateRecipeScreen(
                        navController = navController,
                        recipeViewModel = viewModel(),
                        imageUploadViewModel = viewModel(),
                        userId = userId,
                        userName = userName
                    )
                }
                composable("my_recipes") {
                    MyRecipesScreen(
                        navController = navController,
                        recipeViewModel = viewModel()
                    )
                }
                composable("search") {
                    SearchScreen(
                        navController = navController,
                        searchViewModel = viewModel()
                    )
                }

                // Category screen
                composable(
                    "category/{category}",
                    arguments = listOf(navArgument("category") { type = NavType.StringType })
                ) { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    CategoryScreen(
                        category = category,
                        navController = navController,
                        recipeViewModel = viewModel()
                    )
                }

                // Blog screens
                composable(
                    "blog_detail/{blogId}",
                    arguments = listOf(navArgument("blogId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val blogId = backStackEntry.arguments?.getString("blogId") ?: ""
                    BlogScreen(
                        navController = navController,
                        blogId = blogId,
                        blogViewModel = viewModel()
                    )
                }
                composable("create_blog") {
                    // Get current user ID from AuthViewModel
                    val userId = currentUser?.uid ?: ""
                    val userName = currentUser?.displayName ?: "Anonymous"

                    CreateBlogScreen(
                        navController = navController,
                        blogViewModel = viewModel(),
                        imageUploadViewModel = viewModel(),
//                        userId = userId,
//                        userName = userName
                    )
                }
                composable("my_blogs") {
                    // Get current user ID from AuthViewModel
                    val userId = currentUser?.uid ?: ""

                    MyBlogsScreen(
                        navController = navController,
                        blogViewModel = viewModel(),
                        userId = userId
                    )
                }

                // Meal planner screens
                composable("add_meal/{date}/{mealType}",
                    arguments = listOf(
                        navArgument("date") { type = NavType.StringType },
                        navArgument("mealType") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val date = backStackEntry.arguments?.getString("date") ?: ""
                    val mealType = backStackEntry.arguments?.getString("mealType") ?: ""
                    AddMealScreen(
                        date = date,
                        mealType = mealType,
                        navController = navController,
                        mealPlanViewModel = viewModel(),
                        recipeViewModel = viewModel()
                    )
                }

                // YouTube video screens
                composable("video_search") {
                    VideoSearchScreen(
                        navController = navController
                    )
                }
                composable(
                    "youtube_player/{videoId}",
                    arguments = listOf(navArgument("videoId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
                    YouTubePlayerScreen(
                        videoId = videoId,
                        navController = navController
                    )
                }

                // Food recognition screen
                composable("food_recognition") {
                    FoodRecognitionScreen(
                        navController = navController,
                        recipeViewModel = viewModel()
                    )
                }

                // Admin Panel
                composable("admin_panel") {
                    AdminPanel(
                        navController = navController,
                        authViewModel = authViewModel,
                        recipeViewModel = viewModel(),
                        blogViewModel = viewModel(),
                        userViewModel = viewModel()
                    )
                }
                composable("chat") {
                    ChatScreen(
                        navController = navController,
                        apiKey = "EOOIHIHI12E"
                    )
                }

                // New refrigerator scan screen
                composable("refrigerator_scan") {
                    RefrigeratorScanScreen(
                        navController = navController,
//                        recipeViewModel = viewModel()
                    )
                }
                // New refrigerator scan screen
                composable("admin_blog") {
                    BlogsManageScreen(
                        navController = navController,
//                        recipeViewModel = viewModel()
                    )
                }

                composable("admin_recipe") {
                    RecipesManageScreen(
                        navController = navController,
//                        recipeViewModel = viewModel()
                    )
                }
            }
        }
    }
}
