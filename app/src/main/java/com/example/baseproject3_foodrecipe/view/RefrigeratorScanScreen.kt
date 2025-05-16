package com.example.baseproject3_foodrecipe.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.baseproject3_foodrecipe.R
import com.example.baseproject3_foodrecipe.ml.FoodRecognitionHelper
import com.example.baseproject3_foodrecipe.model.Recipe
import com.example.baseproject3_foodrecipe.viewmodel.RecipeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

private const val TAG = "RefrigeratorScanScreen"

// Data class for detected food items
data class DetectedFoodItem(
    val name: String,
    val confidence: Float,
    val quantity: String,
    val detectedAt: Long = System.currentTimeMillis()
)

// Function to generate quantity string based on food type
fun generateQuantityForFood(foodName: String): String {
    return when {
        foodName.contains("potato", ignoreCase = true) -> "${Random.nextInt(1, 6)} pieces"
        foodName.contains("tomato", ignoreCase = true) -> "${Random.nextInt(1, 8)} pieces"
        foodName.contains("carrot", ignoreCase = true) -> "${Random.nextInt(1, 8)} pieces"
        foodName.contains("chicken", ignoreCase = true) -> "${Random.nextInt(200, 800)}g"
        foodName.contains("fish", ignoreCase = true) -> "${Random.nextInt(200, 800)}g"
        foodName.contains("fork", ignoreCase = true) -> "${Random.nextInt(200, 800)}g"
        foodName.contains("beef", ignoreCase = true) -> "${Random.nextInt(200, 600)}g"
        foodName.contains("milk", ignoreCase = true) -> "${Random.nextInt(1, 3)}L"
        foodName.contains("cheese", ignoreCase = true) -> "${Random.nextInt(100, 500)}g"
        foodName.contains("egg", ignoreCase = true) -> "${Random.nextInt(4, 12)} pieces"
        foodName.contains("bread", ignoreCase = true) -> "${Random.nextInt(1, 3)} loaves"
        foodName.contains("rice", ignoreCase = true) -> "${Random.nextInt(1, 5)}kg"
        foodName.contains("pasta", ignoreCase = true) -> "${Random.nextInt(200, 800)}g"
        foodName.contains("vegetable", ignoreCase = true) -> "${Random.nextInt(200, 800)}g"
        foodName.contains("fruit", ignoreCase = true) -> "${Random.nextInt(200, 800)}g"
        else -> "${Random.nextInt(100, 500)}g"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefrigeratorScanScreen(
    navController: NavController,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State for ESP32-CAM connection
    var showIpDialog by remember { mutableStateOf(false) }
    var ipAddress by remember { mutableStateOf(TextFieldValue("192.168.1.")) }
    var isConnecting by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    var connectionError by remember { mutableStateOf<String?>(null) }
    var debugInfo by remember { mutableStateOf<String?>(null) }

    // State for camera image
    var cameraImage by remember { mutableStateOf<Bitmap?>(null) }

    // State for scanning process
    var isScanning by remember { mutableStateOf(false) }
    var isDetecting by remember { mutableStateOf(false) }

    // State for detected food items
    var detectedFoodItems by remember { mutableStateOf<List<DetectedFoodItem>>(emptyList()) }

    // State for recommended recipes based on detected items
    var recommendedRecipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }

    // Initialize FoodRecognitionHelper
    val foodRecognitionHelper = remember { FoodRecognitionHelper(context) }

    // Load recipes when the screen is first displayed
    LaunchedEffect(Unit) {
        recipeViewModel.loadAllRecipes()
    }

    // Function to find recipes that use the detected ingredients
    fun findRecommendedRecipes(ingredients: List<String>) {
        coroutineScope.launch {
            try {
                // Get all recipes from the view model
                val allRecipes = recipeViewModel.recipes.value

                // Filter recipes that use at least one of the detected ingredients
                val matchingRecipes = allRecipes.filter { recipe ->
                    recipe.ingredients.any { ingredient ->
                        ingredients.any { detectedIngredient ->
                            ingredient.contains(detectedIngredient, ignoreCase = true) ||
                                    detectedIngredient.contains(ingredient, ignoreCase = true)
                        }
                    }
                }

                // If we have matching recipes, use them; otherwise, use some default recipes
                recommendedRecipes = if (matchingRecipes.isNotEmpty()) {
                    matchingRecipes.take(5)
                } else emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error finding recommended recipes", e)
                recommendedRecipes = emptyList()
            }
        }
    }

    // Function to detect food in the captured image using the TFLite model
    fun detectFoodInImage(bitmap: Bitmap) {
        isDetecting = true

        coroutineScope.launch {
            try {
                // Use the actual FoodRecognitionHelper to detect food in the image
                val recognitionResults = withContext(Dispatchers.Default) {
                    foodRecognitionHelper.detectFood(bitmap)
                }

                // Process the results
                if (recognitionResults.isNotEmpty()) {
                    // Convert the results to DetectedFoodItem objects
                    val foodItems = recognitionResults.map { result ->
                        // Generate quantity and expiry days based on the food type
                        val quantity = generateQuantityForFood(result.label)

                        DetectedFoodItem(
                            name = result.label,
                            confidence = result.confidence,
                            quantity = quantity,
                        )
                    }

                    detectedFoodItems = foodItems
                    detectedFoodItems = detectedFoodItems.distinctBy { it.name } 

                    // Find recipes that use the detected ingredients
                    findRecommendedRecipes(foodItems.map { it.name })
                } else {
                    connectionError = "No food items detected in the image"
                    detectedFoodItems = emptyList()
                    recommendedRecipes = emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error detecting food", e)
                connectionError = "Error detecting food: ${e.message ?: "Unknown error"}"
                detectedFoodItems = emptyList()
                recommendedRecipes = emptyList()
            } finally {
                isDetecting = false
            }
        }
    }

    // Function to test connection to ESP32-CAM
    fun testConnection() {
        isConnecting = true
        connectionError = null
        debugInfo = null

        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val url = URL("http://${ipAddress.text}")
                    Log.d(TAG, "Testing connection to: $url")

                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000 // 5 seconds timeout
                    connection.readTimeout = 5000
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("Connection", "close")

                    try {
                        val responseCode = connection.responseCode
                        Log.d(TAG, "Connection test response code: $responseCode")

                        // Read response for debugging
                        val reader = BufferedReader(
                            InputStreamReader(
                                if (responseCode >= 400) connection.errorStream else connection.inputStream
                            )
                        )
                        val response = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line).append('\n')
                        }

                        debugInfo = "Response: $responseCode\n${response.toString().take(200)}"
                        Log.d(TAG, "Response: $response")

                        // Even if we get a 404, consider it connected if we can reach the server
                        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                            isConnected = true
                            connectionError = null
                        } else {
                            isConnected = false
                            connectionError = "Connection test failed: HTTP $responseCode"
                        }
                    } finally {
                        connection.disconnect()
                    }
                }
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Connection timed out", e)
                isConnected = false
                connectionError =
                    "Connection timed out. Please check the IP address and ensure the ESP32-CAM is powered on."
            } catch (e: Exception) {
                Log.e(TAG, "Connection error", e)
                isConnected = false
                connectionError = "Connection error: ${e.message ?: "Unknown error"}"
            } finally {
                isConnecting = false
            }
        }
    }

    fun askAIForRecipes() {
        navController.navigate("chat?message=")
    }

    // Function to capture photo from ESP32-CAM
    fun capturePhoto() {
        if (!isConnected) {
            connectionError = "Not connected to ESP32-CAM"
            return
        }

        isScanning = true
        connectionError = null
        debugInfo = null

        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // First, try to get the photo
                    val photoUrl = URL("http://${ipAddress.text}/photo")
                    Log.d(TAG, "Requesting photo from: $photoUrl")

                    val connection = photoUrl.openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000 // 10 seconds timeout
                    connection.readTimeout = 10000
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("Connection", "close")

                    try {
                        val responseCode = connection.responseCode
                        Log.d(TAG, "Photo request response code: $responseCode")

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            // Get content type to verify it's an image
                            val contentType = connection.contentType
                            Log.d(TAG, "Content-Type: $contentType")

                            if (contentType != null && contentType.startsWith("image/")) {
                                val inputStream: InputStream = connection.inputStream
                                cameraImage = BitmapFactory.decodeStream(inputStream)

                                if (cameraImage == null) {
                                    Log.e(TAG, "Failed to decode image")
                                    connectionError = "Failed to decode image from camera"
                                } else {
                                    Log.d(
                                        TAG,
                                        "Successfully decoded image: ${cameraImage!!.width}x${cameraImage!!.height}"
                                    )

                                    // Now that we have the image, start food detection
                                    isScanning = false
                                    detectFoodInImage(cameraImage!!)
                                }
                            } else {
                                // Read response for debugging
                                val reader =
                                    BufferedReader(InputStreamReader(connection.inputStream))
                                val response = StringBuilder()
                                var line: String?
                                while (reader.readLine().also { line = it } != null) {
                                    response.append(line).append('\n')
                                }

                                Log.e(
                                    TAG,
                                    "Unexpected content type: $contentType, Response: $response"
                                )
                                debugInfo = "Unexpected content type: $contentType\nResponse: ${
                                    response.toString().take(200)
                                }"
                                connectionError = "Server did not return an image"
                            }
                        } else {
                            // Read error response for debugging
                            val reader = BufferedReader(InputStreamReader(connection.errorStream))
                            val response = StringBuilder()
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                response.append(line).append('\n')
                            }

                            Log.e(TAG, "Error response: $response")
                            debugInfo = "Error response: ${response.toString().take(200)}"
                            connectionError = "Failed to capture photo: HTTP $responseCode"
                        }
                    } finally {
                        connection.disconnect()
                    }
                }
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Request timed out", e)
                connectionError = "Request timed out. The ESP32-CAM may be busy or unresponsive."
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing photo", e)
                connectionError = "Error capturing photo: ${e.message ?: "Unknown error"}"
            } finally {
                isScanning = false
            }
        }
    }

    // Function to create default recipes based on detected ingredients

    // IP Address Dialog
    if (showIpDialog) {
        Dialog(onDismissRequest = { showIpDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Connect to ESP32-CAM",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("IP Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showIpDialog = false }) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                showIpDialog = false
                                testConnection()
                            }
                        ) {
                            Text("Connect")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refrigerator Contents") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Connection status indicator
                    if (isConnected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Connected",
                            tint = Color.Green,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    // Connect button
                    IconButton(onClick = { showIpDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Connect to ESP32-CAM"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFA000),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection status message
            item {
                if (connectionError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        )
                    ) {
                        Text(
                            text = connectionError!!,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = Color.Red
                        )
                    }
                }

                if (debugInfo != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Text(
                            text = "Debug Info: $debugInfo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = Color.DarkGray,
                            fontSize = 12.sp
                        )
                    }
                }

                if (isConnecting) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Connecting to ESP32-CAM at ${ipAddress.text}...",
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }

                if (isConnected && !isConnecting && connectionError == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Connected",
                                tint = Color.Green
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Connected to ESP32-CAM at ${ipAddress.text}",
                                color = Color(0xFF388E3C)
                            )
                        }
                    }
                }
            }

            // Camera feed / image placeholder
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (cameraImage != null) {
                        // Show the captured image
                        Image(
                            bitmap = cameraImage!!.asImageBitmap(),
                            contentDescription = "Captured Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (isScanning || isDetecting) {
                            // Show scanning/detecting overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (isScanning)
                                            "Capturing image from ESP32-CAM..."
                                        else
                                            "Detecting food items...",
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Show placeholder
                        Image(
                            painter = painterResource(id = R.drawable.placeholder_image),
                            contentDescription = "Refrigerator Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Semi-transparent overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isConnected) {
                                Text(
                                    text = "Ready to capture image",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Connect to ESP32-CAM",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Capture button
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isConnected && !isScanning && !isDetecting) {
                            capturePhoto()
                        } else if (!isConnected) {
                            showIpDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected && !isScanning && !isDetecting) Color(
                            0xFFFF7043
                        ) else Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = isConnected && !isScanning && !isDetecting
                ) {
                    if (isScanning || isDetecting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isScanning -> "Capturing..."
                            isDetecting -> "Detecting..."
                            !isConnected -> "Connect to ESP32-CAM first"
                            else -> "Capture and detect food"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Detected food items section
            item {
                Text(
                    text = "Detected Food Items",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (detectedFoodItems.isEmpty() && !isDetecting && cameraImage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF8E1)
                        )
                    ) {
                        Text(
                            text = "No food items detected in the image. Try capturing another image.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = Color(0xFFF57C00)
                        )
                    }
                }
            }

            // List of detected food items
            items(detectedFoodItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFE0B2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.name.first().toString(),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF7043)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Confidence indicator
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    item.confidence > 0.8f -> Color(0xFF4CAF50)
                                    item.confidence > 0.6f -> Color(0xFFFFC107)
                                    else -> Color(0xFFFF5722)
                                }
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${(item.confidence * 100).toInt()}%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Recommended recipes section
            item {
                if (detectedFoodItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Recommended Recipes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (recommendedRecipes.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Text(
                                text = "Finding recipes that match your ingredients...",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recommendedRecipes) { recipe ->
                                RecommendedRecipeCard(
                                    recipe = recipe,
                                    onClick = {
                                        navController.navigate("recipe_detail/${recipe.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                if (!detectedFoodItems.isEmpty()) {
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

@Composable
fun RecipeCard(recipe: Recipe, navController: NavController) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(220.dp)
            .padding(vertical = 4.dp)
            .clickable {
                // Navigate to recipe detail screen when clicked
                navController.navigate("recipe_detail/${recipe.id}")
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Recipe image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (recipe.imageUrl.isNotEmpty()) {
                    // If the recipe has an image URL, load it (implementation not shown)
                    // For now, just show a placeholder
                    Image(
                        painter = painterResource(id = R.drawable.placeholder_image),
                        contentDescription = recipe.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.placeholder_image),
                        contentDescription = recipe.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Recipe details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "by ${recipe.authorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${recipe.prepTime} min • ${recipe.difficulty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF7043)
                )
            }
        }
    }
}
