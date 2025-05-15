package com.example.baseproject3_foodrecipe.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.baseproject3_foodrecipe.ml.FoodRecognitionHelper
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import androidx.compose.ui.graphics.Color as ComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodRecognitionScreen(
    navController: NavController,
    recipeViewModel: RecipeViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    )}

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Request camera permission if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Initialize FoodRecognitionHelper
    val foodRecognitionHelper = remember { FoodRecognitionHelper(context) }

    // Clean up when screen is destroyed
    DisposableEffect(Unit) {
        onDispose {
            foodRecognitionHelper.close()
        }
    }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var previewUseCase: Preview? by remember { mutableStateOf(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var detections by remember { mutableStateOf<List<FoodRecognitionHelper.Detection>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var showRecipeDialog by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State for recipe recommendations
    var isLoadingRecipes by remember { mutableStateOf(false) }
    var recommendedRecipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var selectedDetection by remember { mutableStateOf<FoodRecognitionHelper.Detection?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Clean up executor when screen is destroyed
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Load recommended recipes when a food item is selected
    LaunchedEffect(selectedDetection) {
        selectedDetection?.let { detection ->
            try {
                isLoadingRecipes = true
                Log.d("FoodRecognition", "Finding recipes for: ${detection.displayName}")

                // Search for recipes containing the detected food
                val recipes = recipeViewModel.searchRecipesByIngredient(detection.displayName)
                recommendedRecipes = recipes

                Log.d("FoodRecognition", "Found ${recipes.size} recipes for ${detection.displayName}")
            } catch (e: Exception) {
                Log.e("FoodRecognition", "Error loading recommended recipes", e)
            } finally {
                isLoadingRecipes = false
            }
        }
    }

    // Function to format detected ingredients into a comma-separated list
    fun getDetectedIngredientsText(): String {
        return if (detections.isEmpty()) {
            "không có nguyên liệu nào"
        } else {
            detections.joinToString(", ") { it.displayName }
        }
    }

    // Function to navigate to chat screen with pre-filled message
    fun askAIForRecipes() {
        val ingredients = getDetectedIngredientsText()
        val message = "Tôi có thể nấu món gì với $ingredients?"
        navController.navigate("chat?message=$message")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nhận diện thực phẩm") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (capturedBitmap == null) {
                // Display camera
                if (hasCameraPermission) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onUseCase = { preview, imageCaptureUseCase ->
                            previewUseCase = preview
                            imageCapture = imageCaptureUseCase
                        },
                        cameraSelector = cameraSelector
                    )

                    // Capture button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val imageCapture = imageCapture ?: return@IconButton

                                isProcessing = true
                                errorMessage = null

                                imageCapture.takePicture(
                                    cameraExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            try {
                                                // Convert ImageProxy to Bitmap
                                                val bitmap = image.toBitmap()

                                                // Rotate bitmap if needed
                                                val matrix = Matrix().apply {
                                                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                                                }

                                                val rotatedBitmap = Bitmap.createBitmap(
                                                    bitmap,
                                                    0,
                                                    0,
                                                    bitmap.width,
                                                    bitmap.height,
                                                    matrix,
                                                    true
                                                )

                                                // Update UI
                                                capturedBitmap = rotatedBitmap

                                                // Recognize food in coroutine
                                                coroutineScope.launch {
                                                    try {
                                                        val results = withContext(Dispatchers.Default) {
                                                            foodRecognitionHelper.detectFood(rotatedBitmap)
                                                        }

                                                        detections = results

                                                        // Draw bounding box on image
                                                        if (results.isNotEmpty()) {
                                                            processedBitmap = drawDetections(rotatedBitmap, results)

                                                            // Auto-select the first detection with highest confidence
                                                            if (results.isNotEmpty()) {
                                                                selectedDetection = results.first()
                                                            }
                                                        }

                                                    } catch (e: Exception) {
                                                        Log.e("FoodRecognition", "Error recognizing food", e)
                                                        errorMessage = "Lỗi khi nhận diện: ${e.message}"
                                                    } finally {
                                                        isProcessing = false
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e("FoodRecognition", "Error processing image", e)
                                                errorMessage = "Lỗi khi xử lý ảnh: ${e.message}"
                                                isProcessing = false
                                            } finally {
                                                image.close()
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            Log.e("FoodRecognition", "Image capture failed", exception)
                                            errorMessage = "Lỗi khi chụp ảnh: ${exception.message}"
                                            isProcessing = false
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .background(ComposeColor.White.copy(alpha = 0.3f), CircleShape)
                                .border(2.dp, ComposeColor.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Chụp ảnh",
                                tint = ComposeColor.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Camera switch button
                    IconButton(
                        onClick = {
                            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(ComposeColor.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Đổi camera",
                            tint = ComposeColor.White
                        )
                    }
                } else {
                    // Display permission request message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Cần quyền truy cập camera để sử dụng tính năng này",
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Cấp quyền")
                        }
                    }
                }
            } else {
                // Display captured image and recognition results
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        // Display captured image with bounding box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            val bitmapToShow = processedBitmap ?: capturedBitmap
                            bitmapToShow?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Ảnh đã chụp",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            // Close button to return to camera
                            IconButton(
                                onClick = {
                                    capturedBitmap = null
                                    processedBitmap = null
                                    detections = emptyList()
                                    selectedDetection = null
                                    recommendedRecipes = emptyList()
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(ComposeColor.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Đóng",
                                    tint = ComposeColor.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Display recognition results
                    item {
                        Text(
                            text = "Kết quả nhận diện:",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isProcessing) {
                            // Display loading indicator
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Đang nhận diện thực phẩm...")
                            }
                        } else if (errorMessage != null) {
                            // Display error
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    color = ComposeColor.Red,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        capturedBitmap = null
                                        processedBitmap = null
                                        errorMessage = null
                                    }
                                ) {
                                    Text("Thử lại")
                                }
                            }
                        } else if (detections.isEmpty()) {
                            // No results found
                            Text(
                                text = "Không thể nhận diện thực phẩm trong ảnh. Vui lòng thử lại với ảnh khác.",
                                textAlign = TextAlign.Center,
                                color = ComposeColor.Gray
                            )
                        } else {
                            // Display list of results
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(detections) { detection ->
                                    DetectionChip(
                                        detection = detection,
                                        isSelected = selectedDetection == detection,
                                        onClick = {
                                            selectedDetection = detection
                                        }
                                    )
                                }
                            }

                            // Show the selected detection details
                            selectedDetection?.let { detection ->
                                Spacer(modifier = Modifier.height(16.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = detection.displayName,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Độ tin cậy: ${detection.confidencePercentage}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = ComposeColor.Gray
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LinearProgressIndicator(
                                            progress = detection.confidence,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                navController.navigate("search?query=${detection.displayName}")
                                            },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Text("Tìm công thức")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.Search, contentDescription = null)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Recipe recommendations section
                    item {
                        if (selectedDetection != null) {
                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gợi ý món ăn từ ${selectedDetection?.displayName ?: ""}:",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isLoadingRecipes) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Đang tìm công thức phù hợp...")
                                }
                            } else if (recommendedRecipes.isEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Không tìm thấy công thức nào chứa ${selectedDetection?.displayName}",
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = {
                                                navController.navigate("search?query=${selectedDetection?.displayName}")
                                            }
                                        ) {
                                            Text("Tìm công thức")
                                        }
                                    }
                                }
                            } else {
                                recommendedRecipes.forEach { recipe ->
                                    RecommendedRecipeCard(
                                        recipe = recipe,
                                        onClick = {
                                            navController.navigate("recipe_detail/${recipe.id}")
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(
                                    onClick = {
                                        navController.navigate("search?query=${selectedDetection?.displayName}")
                                    },
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                ) {
                                    Text("Xem thêm công thức")
                                }
                            }
                        }
                    }

                    // Add "Ask AI" button at the bottom
                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        if (!detections.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Hỏi AI về các món có thể nấu",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Nguyên liệu đã phát hiện: ${getDetectedIngredientsText()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = { askAIForRecipes() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Chat,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Hỏi AI làm món gì với nguyên liệu này")
                                    }
                                }
                            }

                            // Add some space at the bottom
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetectionChip(
    detection: FoodRecognitionHelper.Detection,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = detection.displayName,
                color = if (isSelected) ComposeColor.White else ComposeColor.Black,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun RecommendedRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            // Recipe image
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp)
            ) {
                if (recipe.imageUrl.isNotEmpty()) {
                    if (recipe.imageUrl.startsWith("http")) {
                        // Load from remote URL
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(recipe.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = recipe.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Load from local storage
                        var bitmap by remember { mutableStateOf<Bitmap?>(null) }

                        LaunchedEffect(recipe.imageUrl) {
                            try {
                                bitmap = LocalImageStorage.loadImage(context, recipe.imageUrl)
                            } catch (e: Exception) {
                                Log.e("RecipeCard", "Error loading image", e)
                            }
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = recipe.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(ComposeColor.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = recipe.name.take(1),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                } else {
                    // Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ComposeColor.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recipe.name.take(1),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            // Recipe details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Thời gian: ${recipe.totalTime} phút",
                    style = MaterialTheme.typography.bodySmall,
                    color = ComposeColor.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (recipe.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Đánh giá: ${recipe.rating}★",
                            style = MaterialTheme.typography.bodySmall,
                            color = ComposeColor(0xFFFFA000)
                        )
                    }
                }
            }
        }
    }
}

// Draw bounding box on image
private fun drawDetections(
    bitmap: Bitmap,
    detections: List<FoodRecognitionHelper.Detection>
): Bitmap {
    // Create a copy of bitmap to draw on
    val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(resultBitmap)

    // Create paint for drawing bounding box
    val boxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    // Create paint for drawing text
    val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        style = Paint.Style.FILL
    }

    // Create paint for text background
    val textBackgroundPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        alpha = 180
    }

    // Draw each detection
    for (detection in detections) {
        val box = detection.boundingBox

        // Draw bounding box
        canvas.drawRect(box, boxPaint)

        // Prepare text
        val text = "${detection.displayName} ${detection.confidencePercentage}"
        val textWidth = textPaint.measureText(text)
        val textHeight = 50f

        // Draw background for text
        canvas.drawRect(
            box.left,
            box.top - textHeight,
            box.left + textWidth + 10,
            box.top,
            textBackgroundPaint
        )

        // Draw text
        canvas.drawText(
            text,
            box.left + 5,
            box.top - 10,
            textPaint
        )
    }

    return resultBitmap
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onUseCase: (Preview, ImageCapture) -> Unit,
    cameraSelector: CameraSelector
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(cameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        // Unbind all use cases before rebinding
        cameraProvider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            onUseCase(preview, imageCapture)
        } catch (e: Exception) {
            Log.e("CameraPreview", "Use case binding failed", e)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

private fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
