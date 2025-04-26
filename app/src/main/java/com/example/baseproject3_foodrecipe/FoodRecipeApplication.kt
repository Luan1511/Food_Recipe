package com.example.baseproject3_foodrecipe

import android.app.Application
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FoodRecipeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            // Check Google Play Services availability
            val availability = GoogleApiAvailability.getInstance()
            val resultCode = availability.isGooglePlayServicesAvailable(this)
            if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                Log.w("FoodRecipeApp", "Google Play Services not available (code $resultCode)")
                // We'll continue anyway, but Firebase might not work correctly
            }

            // Initialize Firebase with explicit options
            if (!FirebaseApp.getApps(this).isEmpty()) {
                Log.d("FoodRecipeApp", "Firebase already initialized")
            } else {
                try {
                    // Initialize Firebase
                    FirebaseApp.initializeApp(this)
                    Log.d("FoodRecipeApp", "Firebase initialized successfully")

                    // Configure Firestore for offline persistence
                    try {
                        val settings = FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(true)
                            .build()
                        FirebaseFirestore.getInstance().firestoreSettings = settings
                        Log.d("FoodRecipeApp", "Firestore settings configured successfully")
                    } catch (e: Exception) {
                        Log.e("FoodRecipeApp", "Error configuring Firestore settings: ${e.message}", e)
                    }
                } catch (e: Exception) {
                    Log.e("FoodRecipeApp", "Error initializing Firebase: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("FoodRecipeApp", "Error in application initialization: ${e.message}", e)
        }
    }
}
