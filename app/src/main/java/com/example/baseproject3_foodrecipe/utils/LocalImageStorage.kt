package com.example.baseproject3_foodrecipe.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Utility class for handling local image storage
 */
object LocalImageStorage {
    // Constants
    const val RECIPE = "recipes"
    const val BLOG = "blogs"
    const val USER = "users"
    const val TEMP = "temp"
    const val PROFILE = "profile"

    private const val TAG = "LocalImageStorage"

    /**
     * Save an image from URI to local storage
     */
    suspend fun saveImage(context: Context, uri: Uri, folder: String): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Create directory if it doesn't exist
            val directory = File(context.filesDir, folder)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Generate a unique filename
            val filename = UUID.randomUUID().toString() + ".jpg"
            val file = File(directory, filename)

            // Save the bitmap to the file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            // Return the relative path
            val path = "$folder/$filename"
            Log.d(TAG, "Image saved to: $path")
            return@withContext path
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image: ${e.message}", e)
            throw e
        }
    }

    /**
     * Load an image from local storage
     */
    suspend fun loadImage(context: Context, path: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading image from: $path")

            // Handle both absolute and relative paths
            val file = if (path.startsWith("/")) {
                File(path)
            } else {
                File(context.filesDir, path)
            }

            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                Log.d(TAG, "Image loaded successfully")
                return@withContext bitmap
            } else {
                Log.e(TAG, "Image file not found: ${file.absolutePath}")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image: ${e.message}", e)
            return@withContext null
        }
    }

    fun fileExists(context: Context, path: String): Boolean {
        return if (path.isEmpty()) false else File(context.filesDir, path).exists()
    }

    /**
     * Delete an image from local storage
     */
    suspend fun deleteImage(context: Context, path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting image: $path")

            // Handle both absolute and relative paths
            val file = if (path.startsWith("/")) {
                File(path)
            } else {
                File(context.filesDir, path)
            }

            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "Image deleted successfully")
                } else {
                    Log.e(TAG, "Failed to delete image")
                }
                return@withContext deleted
            } else {
                Log.e(TAG, "Image file not found: ${file.absolutePath}")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image: ${e.message}", e)
            return@withContext false
        }
    }
}
