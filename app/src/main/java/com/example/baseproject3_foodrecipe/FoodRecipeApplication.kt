package com.example.baseproject3_foodrecipe

import android.app.Application
import android.content.Context

class FoodRecipeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}
