package com.example.vietnam_travel_itinerary_android.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.max

object AvatarImageUtils {
    private const val MAX_EDGE = 1024
    private const val JPEG_QUALITY = 80

    fun uriToAvatarBytes(context: Context, uri: Uri): ByteArray {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val original = BitmapFactory.decodeStream(input)
                ?: throw IllegalArgumentException("Không đọc được ảnh")
            val scaled = scaleDown(original, MAX_EDGE)
            val output = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            if (scaled !== original) scaled.recycle()
            original.recycle()
            return output.toByteArray()
        } ?: throw IllegalArgumentException("Không đọc được ảnh")
    }

    private fun scaleDown(source: Bitmap, maxEdge: Int): Bitmap {
        val width = source.width
        val height = source.height
        val largest = max(width, height)
        if (largest <= maxEdge) return source

        val ratio = maxEdge.toFloat() / largest
        val targetWidth = (width * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }
}
