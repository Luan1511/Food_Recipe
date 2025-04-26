package com.example.baseproject3_foodrecipe.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    // Kiểm tra người dùng hiện tại
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Đăng ký người dùng mới
    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        isChef: Boolean = false
    ): Result<FirebaseUser> {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // Cập nhật tên hiển thị
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    firebaseUser.updateProfile(profileUpdates).await()

                    // Tạo đối tượng User trong Firestore
                    val user = User(
                        id = firebaseUser.uid,
                        name = name,
                        email = email,
                        username = email.substringBefore("@"),
                        chef = isChef,
                        chefTitle = if (isChef) "Chef" else "",
                        bio = "Chào mừng đến với hồ sơ của tôi!"
                    )

                    userRepository.createUser(user)
                    Result.success(firebaseUser)
                } else {
                    Result.failure(Exception("Đăng ký thất bại: Không thể tạo người dùng"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Đăng ký thất bại: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Đăng nhập
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    Result.success(firebaseUser)
                } else {
                    Result.failure(Exception("Đăng nhập thất bại: Không tìm thấy người dùng"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Đăng nhập thất bại: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Đăng xuất
    fun logoutUser() {
        auth.signOut()
    }

    // Gửi email đặt lại mật khẩu
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                auth.sendPasswordResetEmail(email).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Gửi email đặt lại mật khẩu thất bại: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Cập nhật thông tin người dùng
    suspend fun updateUserProfile(name: String, photoUrl: String? = null): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val user = auth.currentUser
                if (user != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .apply {
                            if (photoUrl != null) {
                                setPhotoUri(android.net.Uri.parse(photoUrl))
                            }
                        }
                        .build()

                    user.updateProfile(profileUpdates).await()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Cập nhật thất bại: Người dùng chưa đăng nhập"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Cập nhật thông tin người dùng thất bại: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Cập nhật email
    suspend fun updateEmail(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val user = auth.currentUser
                if (user != null) {
                    user.updateEmail(email).await()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Cập nhật email thất bại: Người dùng chưa đăng nhập"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Cập nhật email thất bại: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Cập nhật mật khẩu
    suspend fun updatePassword(password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val user = auth.currentUser
                if (user != null) {
                    user.updatePassword(password).await()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Cập nhật mật khẩu thất bại: Người dùng chưa đăng nhập"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Cập nhật mật khẩu thất bại: ${e.message}")
                Result.failure(e)
            }
        }
    }
}
