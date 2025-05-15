package com.example.baseproject3_foodrecipe.view

import android.content.Context
import android.net.Uri
import com.example.baseproject3_foodrecipe.R

object VideoUtils {
    fun getVideoUri(context: Context, videoName: String): Uri {
        val resourceId = when (videoName) {
            "Italian Pasta" -> R.raw.italian_pasta_video
            "Sushi Making" -> R.raw.italian_pasta_video // Fallback to italian_pasta_video since sushi_making_video doesn't exist
            else -> R.raw.italian_pasta_video // Default fallback
        }

        return Uri.parse("android.resource://${context.packageName}/$resourceId")
    }

    // Get a lower resolution video if available
    fun getLowResVideoUri(context: Context, videoName: String): Uri {
        // In a real app, you would have lower resolution versions of your videos
        // For now, we'll just return the same URI
        return getVideoUri(context, videoName)
    }
}
