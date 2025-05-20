package com.example.baseproject3_foodrecipe.view

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeCardFromModel(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    // State for showing confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // State for visual feedback during long press
    var isLongPressed by remember { mutableStateOf(false) }

    // Get haptic feedback
    val haptic = LocalHapticFeedback.current

    val context = LocalContext.current
    var imageRecipe by remember { mutableStateOf<Bitmap?>(null) }

    // Load user image in a coroutine
    LaunchedEffect(Unit) {
        if (recipe.imageUrl.isNotEmpty()) {
            try {
                imageRecipe = LocalImageStorage.loadImage(context, recipe.imageUrl)
            } catch (e: Exception) {
                // Handle error loading image
            }
        }
    }

    // Confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Bookmark") },
            text = { Text("Are you sure you want to remove \"${recipe.name}\" from your bookmarks?") },
            confirmButton = {
                Button(
                    onClick = {
                        // Call the onLongClick function to delete the recipe
                        onLongClick?.invoke()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            icon = {
                Icon(
                    Icons.Default.BookmarkRemove,
                    contentDescription = "Remove Bookmark",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = { onClick() },
                        onLongClick = {
                            // Provide haptic feedback
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                            // Show visual feedback
                            isLongPressed = true

                            // Use a coroutine to reset the visual feedback after a short delay
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(200)
                                isLongPressed = false

                                // Show the confirmation dialog
                                showDeleteDialog = true
                            }
                        }
                    )
                } else {
                    Modifier.clickable { onClick() }
                }
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Recipe Image
            if (imageRecipe != null) {
                Image(
                    bitmap = imageRecipe!!.asImageBitmap(),
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading...",
                        color = Color.White
                    )
                }
            }

            // Gradient overlay for better text visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    // Recipe Title
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Recipe Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Author
                        Text(
                            text = "By ${recipe.authorName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Difficulty
                        Text(
                            text = recipe.difficulty,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Time
                        Text(
                            text = "${recipe.prepTime + recipe.cookTime} min",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White
                            )
                        )
                    }
                }
            }

            // Featured badge
            if (recipe.featured) {
                Badge(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Featured")
                    }
                }
            }

            // Visual feedback overlay when long pressed
            if (isLongPressed && onLongClick != null) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.BookmarkRemove,
                            contentDescription = "Remove Bookmark",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }
}