package com.example.baseproject3_foodrecipe.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun registerUser(email: String, password: String, name: String, isChef: Boolean = false): FirebaseUser? {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Update display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                firebaseUser.updateProfile(profileUpdates).await()

                // Create user in Firestore
                val user = User(
                    id = firebaseUser.uid,
                    name = name,
                    username = email.substringBefore("@"),
                    email = email,
                    isChef = isChef,
                    chefTitle = if (isChef) "Chef" else ""
                )

                userRepository.createUser(user)

                firebaseUser
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration error: ${e.message}")
            null
        }
    }

    suspend fun loginUser(email: String, password: String): FirebaseUser? {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            authResult.user
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error: ${e.message}")
            null
        }
    }

    fun logoutUser() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Password reset error: ${e.message}")
            false
        }
    }
}
