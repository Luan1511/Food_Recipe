package com.example.baseproject3_foodrecipe.view

import android.content.Context
import android.net.Uri
import com.example.baseproject3_foodrecipe.R

object VideoUtils {
    fun getVideoUri(context: Context, videoName: String): Uri {
        val resourceId = when (videoName) {
            "Italian Pasta" -> R.raw.italian_pasta_video
            "Sushi Making" -> R.raw.sushi_making_video
            else -> R.raw.italian_pasta_video // Default fallback
        }

        return Uri.parse("android.resource://${context.packageName}/$resourceId")
    }
}
