package com.example.baseproject3_foodrecipe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.api.YouTubeApiService
import com.example.baseproject3_foodrecipe.model.YouTubeVideo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class YouTubeViewModel : ViewModel() {
    private val youTubeApiService = YouTubeApiService()

    private val _trendingVideos = MutableStateFlow<List<YouTubeVideo>>(emptyList())
    val trendingVideos: StateFlow<List<YouTubeVideo>> = _trendingVideos.asStateFlow()

    private val _searchResults = MutableStateFlow<List<YouTubeVideo>>(emptyList())
    val searchResults: StateFlow<List<YouTubeVideo>> = _searchResults.asStateFlow()

    private val _currentVideo = MutableStateFlow<YouTubeVideo?>(null)
    val currentVideo: StateFlow<YouTubeVideo?> = _currentVideo.asStateFlow()

    private val _relatedVideos = MutableStateFlow<List<YouTubeVideo>>(emptyList())
    val relatedVideos: StateFlow<List<YouTubeVideo>> = _relatedVideos.asStateFlow()

    private val _savedVideos = MutableStateFlow<List<YouTubeVideo>>(emptyList())
    val savedVideos: StateFlow<List<YouTubeVideo>> = _savedVideos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadTrendingVideos()
        loadSavedVideos()
    }

    fun loadTrendingVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val videos = youTubeApiService.getTrendingVideos("cooking")
                _trendingVideos.value = videos
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load trending videos: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun searchVideos(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val videos = youTubeApiService.searchVideos(query)
                _searchResults.value = videos
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search videos: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun loadVideoDetails(videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val video = youTubeApiService.getVideoDetails(videoId)
                _currentVideo.value = video

                // Load related videos
                val related = youTubeApiService.getRelatedVideos(videoId)
                _relatedVideos.value = related

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load video details: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun loadSavedVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val videos = youTubeApiService.getSavedVideos()
                _savedVideos.value = videos
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load saved videos: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun saveVideo(video: YouTubeVideo) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = youTubeApiService.saveVideo(video)
                if (success) {
                    loadSavedVideos()
                } else {
                    _errorMessage.value = "Failed to save video"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error saving video: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Xóa video cho người dùng thông thường (chỉ xóa được video đã lưu)
    fun deleteVideo(videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = youTubeApiService.deleteVideo(videoId)
                if (success) {
                    loadSavedVideos()
                } else {
                    _errorMessage.value = "Failed to delete video"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting video: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Xóa video cho admin (có thể xóa bất kỳ video nào đã lưu)
    fun adminDeleteVideo(videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = youTubeApiService.adminDeleteVideo(videoId)
                if (success) {
                    loadSavedVideos()
                } else {
                    _errorMessage.value = "Failed to delete video"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting video: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
