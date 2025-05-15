package com.example.baseproject3_foodrecipe.view

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.viewmodel.AuthViewModel

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: Int
)

@Composable
fun BottomNavigation(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser = authViewModel.currentUser.collectAsState().value
    val userId = currentUser?.uid ?: ""

    val items = listOf(
        BottomNavItem("home", "Home", R.drawable.ic_home),
        BottomNavItem("planner", "Planner", R.drawable.ic_date_range),
        BottomNavItem("saved", "Saved", R.drawable.ic_saved),
        BottomNavItem("videos", "Videos", R.drawable.ic_video),
        BottomNavItem("blog", "Blog", R.drawable.ic_blog),
        BottomNavItem("profile/$userId", "Profile", R.drawable.ic_profile)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.height(64.dp),
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route ||
                    (item.route.startsWith("profile/") && currentRoute?.startsWith("profile/") == true)

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                selected = selected,
                onClick = {
                    if (!selected) {
                        if (item.route == "profile/$userId" && userId.isEmpty()) {
                            // If user is not logged in, navigate to login
                            navController.navigate("login")
                        } else {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                },
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = Color(0xFF4CAF50),
//                    selectedTextColor = Color(0xFF4CAF50),
//                    indicatorColor = Color(0xFFE8F5E9)
//                )
            )
        }
    }
}
