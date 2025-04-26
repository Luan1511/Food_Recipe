package com.example.baseproject3_foodrecipe.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UploadState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val downloadUrl: String? = null
)

class ImageUploadViewModel : ViewModel() {
    private val storage = FirebaseStorage.getInstance()

    private val _uploadState = MutableStateFlow(UploadState())
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    fun uploadImage(imageUri: Uri, path: String) {
        viewModelScope.launch {
            try {
                _uploadState.value = UploadState(isLoading = true)

                val storageRef = storage.reference.child(path)
                storageRef.putFile(imageUri).await()

                // Get download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()

                _uploadState.value = UploadState(
                    isLoading = false,
                    isSuccess = true,
                    downloadUrl = downloadUrl
                )
            } catch (e: Exception) {
                _uploadState.value = UploadState(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message
                )
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState()
    }
}
