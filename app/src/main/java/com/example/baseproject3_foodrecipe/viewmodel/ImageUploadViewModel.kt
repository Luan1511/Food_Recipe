package com.example.baseproject3_foodrecipe.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.ImageRepository
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling image uploads
 */
class ImageUploadViewModel : ViewModel() {
    private val imageRepository = ImageRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _imagePath = MutableStateFlow("")
    val imagePath: StateFlow<String> = _imagePath.asStateFlow()

    private val TAG = "ImageUploadViewModel"

    /**
     * Upload an image from a URI to Firebase Storage
     */
    fun uploadImage(context: Context, uri: Uri, folder: String) {
        _isLoading.value = true
        _isSuccess.value = false
        _error.value = null

        viewModelScope.launch {
            try {
                // First save to local storage
                Log.d(TAG, "Saving image to local storage")
                val localPath = LocalImageStorage.saveImage(context, uri, folder)
                Log.d(TAG, "Local path: $localPath")

                // In a real app, upload to Firebase Storage and get URL
                // val remoteUrl = imageRepository.uploadImageToFirebase(localPath)

                // For now, just use the local path
                _imagePath.value = localPath
                _isSuccess.value = true
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading image: ${e.message}", e)
                _error.value = e.message
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Set the image path directly (used when an image is already saved)
     */
    fun setImagePath(path: String) {
        _imagePath.value = path
        _isSuccess.value = true
    }

    /**
     * Reset the state
     */
    fun reset() {
        _isLoading.value = false
        _isSuccess.value = false
        _error.value = null
        // Don't reset the image path, as it might be needed later
    }
}
