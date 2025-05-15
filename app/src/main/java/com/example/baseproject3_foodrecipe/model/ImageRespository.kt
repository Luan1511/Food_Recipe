package com.example.baseproject3_foodrecipe.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * Repository for handling image operations
 */
class ImageRepository {
    /**
     * Save an image to the Firebase Storage
     */
    suspend fun uploadImageToFirebase(localImagePath: String): String {
        // In a real app, this would upload to Firebase Storage
        // For now, we'll just use the local path
        return localImagePath
    }

    /**
     * Save a bitmap to a local file
     */
    suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap, folder: String): String = withContext(Dispatchers.IO) {
        val filename = UUID.randomUUID().toString() + ".jpg"
        val directory = File(context.filesDir, folder)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, filename)

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            val path = "$folder/$filename"
            Log.d("ImageRepository", "Image saved to: $path")
            return@withContext path
        } catch (e: IOException) {
            Log.e("ImageRepository", "Error saving bitmap: ${e.message}", e)
            throw e
        }
    }

    /**
     * Load an image from a local file path
     */
    suspend fun loadImageFromPath(context: Context, path: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // First, try to load from app's internal storage
            val file = if (path.startsWith("/")) {
                File(path)
            } else {
                File(context.filesDir, path)
            }

            if (file.exists()) {
                return@withContext BitmapFactory.decodeFile(file.absolutePath)
            }

            // If that fails, try to load from the app's assets
            try {
                context.assets.open(path).use { inputStream ->
                    return@withContext BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: IOException) {
                Log.e("ImageRepository", "Error loading from assets: ${e.message}", e)
                // Continue to try other methods
            }

            // If all else fails, return null
            Log.e("ImageRepository", "Image not found: $path")
            return@withContext null
        } catch (e: Exception) {
            Log.e("ImageRepository", "Error loading image: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Save an image from a URI to local storage
     */
    suspend fun saveImageFromUri(context: Context, uri: Uri, folder: String): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            return@withContext saveBitmapToFile(context, bitmap, folder)
        } catch (e: Exception) {
            Log.e("ImageRepository", "Error saving image from URI: ${e.message}", e)
            throw e
        }
    }
}
