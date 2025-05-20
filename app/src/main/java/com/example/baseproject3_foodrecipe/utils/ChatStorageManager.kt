package com.example.baseproject3_foodrecipe.utils

import android.content.Context
import android.util.Log
import com.example.baseproject3_foodrecipe.view.ChatMessage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class ChatStorageManager(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val TAG = "ChatStorageManager"
    private val fileName = "chat_history.json"

    /**
     * Save the full conversation (overwrite the file)
     */
    fun saveConversation(messages: List<ChatMessage>): Boolean {
        return try {
            val file = getChatFile()
            FileWriter(file).use { writer ->
                gson.toJson(messages, writer)
            }
            Log.d(TAG, "Saved conversation with ${messages.size} messages")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error saving conversation", e)
            false
        }
    }

    /**
     * Load the entire conversation
     */
    fun loadConversation(): List<ChatMessage> {
        val file = getChatFile()
        if (!file.exists()) {
            Log.d(TAG, "No chat history found")
            return emptyList()
        }

        return try {
            FileReader(file).use { reader ->
                val type = object : TypeToken<List<ChatMessage>>() {}.type
                gson.fromJson(reader, type)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error loading conversation", e)
            emptyList()
        }
    }

    /**
     * Add a message to the current conversation
     */
    fun addMessage(message: ChatMessage): Boolean {
        val messages = loadConversation().toMutableList()
        messages.add(message)
        return saveConversation(messages)
    }

    /**
     * Delete the conversation history
     */
    fun deleteConversation(): Boolean {
        val file = getChatFile()
        return if (file.exists()) {
            try {
                val result = file.delete()
                Log.d(TAG, "Deleted chat history: $result")
                result
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting chat history", e)
                false
            }
        } else {
            true
        }
    }

    /**
     * Get the file for saving chat
     */
    private fun getChatFile(): File {
        return File(context.filesDir, fileName)
    }
}
