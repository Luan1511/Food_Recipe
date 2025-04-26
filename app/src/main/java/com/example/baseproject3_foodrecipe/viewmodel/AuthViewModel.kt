package com.example.baseproject3_foodrecipe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.AuthRepository
import com.example.baseproject3_foodrecipe.model.User
import com.example.baseproject3_foodrecipe.model.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        // Kiểm tra trạng thái đăng nhập khi khởi tạo
        checkAuthState()
    }

    private fun checkAuthState() {
        val user = authRepository.getCurrentUser()
        _currentUser.value = user
        _isLoggedIn.value = user != null

        if (user != null) {
            loadUserProfile(user.uid)
        }
    }

    private fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getUser(userId)
                _userProfile.value = user
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Không thể tải thông tin người dùng: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registerUser(email: String, password: String, name: String, isChef: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = authRepository.registerUser(email, password, name, isChef)

                if (result.isSuccess) {
                    _currentUser.value = result.getOrNull()
                    _isLoggedIn.value = true

                    // Tải thông tin người dùng
                    result.getOrNull()?.let { user ->
                        loadUserProfile(user.uid)
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Đăng ký thất bại"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Đăng ký thất bại: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = authRepository.loginUser(email, password)

                if (result.isSuccess) {
                    _currentUser.value = result.getOrNull()
                    _isLoggedIn.value = true

                    // Tải thông tin người dùng
                    result.getOrNull()?.let { user ->
                        loadUserProfile(user.uid)
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Đăng nhập thất bại"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Đăng nhập thất bại: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logoutUser() {
        authRepository.logoutUser()
        _currentUser.value = null
        _userProfile.value = null
        _isLoggedIn.value = false
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = authRepository.sendPasswordResetEmail(email)

                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Gửi email đặt lại mật khẩu thất bại"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gửi email đặt lại mật khẩu thất bại: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
