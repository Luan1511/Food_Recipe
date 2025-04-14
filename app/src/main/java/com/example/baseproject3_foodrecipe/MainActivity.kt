//package com.example.baseproject3_foodrecipe
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.painter.ColorPainter
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import com.example.baseproject3_foodrecipe.ui.theme.FoodRecipeTheme
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.sp
//
//class MainActivity : ComponentActivity() {
//    @OptIn(ExperimentalMaterial3Api::class)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            FoodRecipeTheme {
//                Scaffold(
//                    topBar = {
//                        TopAppBar(
//                            title = { Text("CookLive") },
//                            actions = {
//                                IconButton(onClick = { /* TODO: Triển khai tìm kiếm */ }) {
//                                    Icon(Icons.Default.Search, contentDescription = "Search")
//                                }
//                                IconButton(onClick = { /* TODO: Triển khai cài đặt */ }) {
//                                    Icon(Icons.Default.MoreVert, contentDescription = "Settings")
//                                }
//                            }
//                        )
//                    }
//                    // Phần bottomBar đã được xóa
//                ) { paddingValues ->
//                    LazyColumn(
//                        contentPadding = paddingValues,
//                        modifier = Modifier.fillMaxSize()
//                    ) {
//                        item { LiveNowSection() }
//                        item { PopularRecipesSection() }
//                        item { CategoriesSection() }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Preview(showSystemUi = true)
//@Composable
//fun LiveNowSection() {
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text("Live Now", style = MaterialTheme.typography.headlineSmall)
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            LiveStreamCard(
//                chefName = "Chef Mario",
//                title = "Italian Pasta",
//                modifier = Modifier.weight(1f)
//            )
//            LiveStreamCard(
//                chefName = "Chef Yuki",
//                title = "Sushi Making",
//                modifier = Modifier.weight(1f)
//            )
//        }
//    }
//}
//
//@Composable
//fun LiveStreamCard(chefName: String, title: String, modifier: Modifier = Modifier) {
//    Card(
//        shape = MaterialTheme.shapes.medium,
//        modifier = modifier.padding(8.dp)
//    ) {
//        Column {
//            Box {
//                Image(
//                    painter = ColorPainter(Color.Gray),
//                    contentDescription = "Live Stream",
//                    modifier = Modifier.fillMaxWidth().height(100.dp)
//                )
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.TopStart)
//                        .background(Color.Red, shape = RoundedCornerShape(4.dp))
//                        .padding(horizontal = 4.dp, vertical = 2.dp)
//                ) {
//                    Text("LIVE", color = Color.White, fontSize = 12.sp)
//                }
//            }
//            Text(
//                text = title,
//                style = MaterialTheme.typography.bodyMedium,
//                modifier = Modifier.padding(8.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun PopularRecipesSection() {
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text("Popular Recipes", style = MaterialTheme.typography.headlineSmall)
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            RecipeCard(
//                title = "Butter Chicken",
//                time = "30 min",
//                difficulty = "Medium",
//                modifier = Modifier.weight(1f)
//            )
//            RecipeCard(
//                title = "Chocolate Cake",
//                time = "45 min",
//                difficulty = "Easy",
//                modifier = Modifier.weight(1f)
//            )
//        }
//    }
//}
//
//@Composable
//fun RecipeCard(title: String, time: String, difficulty: String, modifier: Modifier = Modifier) {
//    Card(
//        shape = MaterialTheme.shapes.medium,
//        modifier = modifier.padding(8.dp)
//    ) {
//        Column {
//            Image(
//                painter = ColorPainter(Color.Gray),
//                contentDescription = "Recipe",
//                modifier = Modifier.fillMaxWidth().height(100.dp)
//            )
//            Text(
//                text = title,
//                style = MaterialTheme.typography.bodyMedium,
//                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//            )
//            Text(
//                text = "⏱ $time  🔥 $difficulty",
//                style = MaterialTheme.typography.labelLarge,
//                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun CategoriesSection() {
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text("Categories", style = MaterialTheme.typography.headlineSmall)
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            CategoryButton(
//                icon = Icons.Default.Restaurant,
//                label = "Asian",
//                color = Color(0xFFFF9800) // Cam
//            )
//            CategoryButton(
//                icon = Icons.Default.LocalPizza,
//                label = "Italian",
//                color = Color(0xFFF06292) // Hồng
//            )
//            CategoryButton(
//                icon = Icons.Default.Eco,
//                label = "Vegan",
//                color = Color(0xFF8BC34A) // Xanh lá
//            )
//            CategoryButton(
//                icon = Icons.Default.Cake,
//                label = "Desserts",
//                color = Color(0xFF9C27B0) // Tím
//            )
//        }
//    }
//}
//
//@Composable
//fun CategoryButton(icon: ImageVector, label: String, color: Color) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.padding(8.dp)
//    ) {
//        Box(
//            modifier = Modifier
//                .size(64.dp)
//                .background(color, shape = CircleShape)
//                .clickable { /* TODO: Điều hướng đến danh mục */ },
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = label,
//                tint = Color.White
//            )
//        }
//        Text(
//            text = label,
//            style = MaterialTheme.typography.bodyMedium,
//            textAlign = TextAlign.Center
//        )
//    }
//}