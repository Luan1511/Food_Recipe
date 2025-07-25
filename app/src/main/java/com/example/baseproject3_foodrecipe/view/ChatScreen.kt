package com.example.baseproject3_foodrecipe.view

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.baseproject3_foodrecipe.utils.ChatStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private lateinit var tts: TextToSpeech
private const val TAG = "ChatScreen"

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val errorCode: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    apiKey: String = "sk-abcxyz",
    outMessage: String? = ""
) {
    val context = LocalContext.current
    val chatStorageManager = remember { ChatStorageManager(context) }

    // State for chat messages
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // For auto-scrolling to bottom
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Load saved messages when the screen is first displayed
    LaunchedEffect(Unit) {
        val savedMessages = chatStorageManager.loadConversation()
        if (savedMessages.isNotEmpty()) {
            messages = savedMessages
            Log.d(TAG, "Loaded ${savedMessages.size} messages from storage")
        } else {
            // Add welcome message if no saved messages
            val welcomeMessage = ChatMessage(
                message = "Xin chào! Tôi là trợ lý ẩm thực AI. Bạn có thể hỏi tôi về công thức nấu ăn, mẹo nấu nướng, hoặc bất kỳ điều gì liên quan đến ẩm thực!",
                isFromUser = false
            )
            messages = listOf(welcomeMessage)
            chatStorageManager.saveConversation(messages)
            Log.d(TAG, "No saved messages found, added welcome message")
        }

    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()

            spokenText?.let {
                inputText = it
            }
        }
    }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("vi", "VN")
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Add user message
        val userMessage = ChatMessage(message = text, isFromUser = true)
        messages = messages + userMessage

        // Save the updated conversation
        coroutineScope.launch {
            chatStorageManager.saveConversation(messages)
            Log.d(TAG, "Saved conversation after adding user message")
        }

        inputText = ""
        isLoading = true
        focusManager.clearFocus()

        // Call GPT API
        coroutineScope.launch {
            try {
                // Create OkHttpClient with timeout
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                val r: String = "WU1EsgKis4wTMIAA"

                // Create request body
                val jsonBody = """
                    {
                        "model": "gpt-3.5-turbo",
                        "messages": [
                            {"role": "user", "content": "You are a helpful cooking assistant that specializes in Vietnamese cuisine. Respond in Vietnamese."},
                            {"role": "user", "content": "$text"}
                        ],
                        "temperature": 0.7
                    }
                """.trimIndent()

                val temp: String = "bkFJRw9eVGIZcvHo2nr1"
                val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

                // Create request
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer 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")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                // Execute request
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    // Parse response
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "")

                    // Extract the assistant's message
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val firstChoice = choices.getJSONObject(0)
                        val message = firstChoice.getJSONObject("message")
                        val content = message.getString("content")

                        // Add AI response
                        val aiMessage = ChatMessage(message = content, isFromUser = false)
                        messages = messages + aiMessage

                        // Save the updated conversation with AI response
                        chatStorageManager.saveConversation(messages)
                        Log.d(TAG, "Saved conversation after adding AI response")
                    } else {
                        val aiMessage = ChatMessage(
                            message = "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này. Vui lòng thử lại sau.",
                            isFromUser = false
                        )
                        messages = messages + aiMessage
                        chatStorageManager.saveConversation(messages)
                    }
                } else {
                    // Handle error response
                    val errorCode = response.code
                    val errorMessage = when (errorCode) {
                        429 -> "Đã vượt quá giới hạn yêu cầu. Vui lòng thử lại sau ít phút."
                        500, 502, 503, 504 -> "Máy chủ OpenAI đang gặp sự cố. Vui lòng thử lại sau."
                        else -> "Đã xảy ra lỗi khi kết nối với dịch vụ. Mã lỗi: $errorCode"
                    }

                    val aiMessage = ChatMessage(
                        message = errorMessage,
                        isFromUser = false,
                        isError = true,
                        errorCode = errorCode
                    )
                    messages = messages + aiMessage
                    chatStorageManager.saveConversation(messages)
                }
            } catch (e: Exception) {
                // Handle exceptions
                val aiMessage = ChatMessage(
                    message = "Đã xảy ra lỗi: ${e.localizedMessage ?: e.message ?: "Lỗi không xác định"}",
                    isFromUser = false,
                    isError = true
                )
                messages = messages + aiMessage
                chatStorageManager.saveConversation(messages)
            } finally {
                isLoading = false
            }
        }
    }

    // Function to retry the last user message
    fun retryLastMessage() {
        val lastUserMessage = messages.lastOrNull { it.isFromUser }
        lastUserMessage?.let {
            sendMessage(it.message)
        }
    }

    // Function to clear conversation history
    fun clearConversation() {
        coroutineScope.launch {
            val success = chatStorageManager.deleteConversation()
            if (success) {
                // Add welcome message after clearing
                val welcomeMessage = ChatMessage(
                    message = "Xin chào! Tôi là trợ lý ẩm thực AI. Bạn có thể hỏi tôi về công thức nấu ăn, mẹo nấu nướng, hoặc bất kỳ điều gì liên quan đến ẩm thực!",
                    isFromUser = false
                )
                messages = listOf(welcomeMessage)
                chatStorageManager.saveConversation(messages)
                Toast.makeText(context, "Đã xóa lịch sử trò chuyện", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Không thể xóa lịch sử trò chuyện", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to scroll to bottom when new message is added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Confirmation dialog for clearing chat history
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Xóa lịch sử trò chuyện") },
            text = { Text("Bạn có chắc chắn muốn xóa toàn bộ lịch sử trò chuyện không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        clearConversation()
                    }
                ) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trợ lý ẩm thực AI") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Add clear history button
                    IconButton(onClick = { showClearConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa lịch sử")
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
            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(
                        message = message,
                        onRetry = if (message.isError) { { retryLastMessage() } } else null
                    )
                }

                // Loading indicator
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFE0E0E0))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Text("Đang nhập...", color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }

            // Input area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Nhập tin nhắn...") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            sendMessage(inputText)
                        }),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    // Send button
                    IconButton(
                        onClick = { sendMessage(inputText) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Bạn muốn hỏi gì?")
                                }
                                speechLauncher.launch(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "Thiết bị của bạn không hỗ trợ nhận dạng giọng nói",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3))
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Speech", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onRetry: (() -> Unit)? = null
) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val backgroundColor = when {
        message.isError -> Color(0xFFFFEBEE)
        message.isFromUser -> Color(0xFFE3F2FD)
        else -> Color(0xFFE0E0E0)
    }
    val textColor = when {
        message.isError -> Color(0xFFB71C1C)
        message.isFromUser -> Color(0xFF1565C0)
        else -> Color.DarkGray
    }
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val timeString = dateFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isFromUser) 4.dp else 16.dp
                    )
                )
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = message.message,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Retry Button
                if (message.isError && onRetry != null) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE57373)
                        ),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Retry",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thử lại")
                    }
                }

                // TTS button
                if (!message.isFromUser) {
                    IconButton(
                        onClick = { speak(message.message) },
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.End)
                    ) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = "Đọc",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }

        Text(
            text = timeString,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp),
            textAlign = if (message.isFromUser) TextAlign.End else TextAlign.Start
        )
    }
}

fun speak(text: String) {
    if (::tts.isInitialized) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}

