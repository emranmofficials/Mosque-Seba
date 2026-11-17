package com.mosqueseba.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun savePickedImageToAppStorage(
    context: Context,
    uri: Uri,
    prefix: String,
    maxDim: Int = 512
): Pair<String?, ImageBitmap?> = withContext(Dispatchers.IO) {
    try {
        val uploadDir = File(context.filesDir, "uploads").apply { mkdirs() }
        val targetFile = File(uploadDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        } ?: return@withContext null to null

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(targetFile.absolutePath, boundsOptions)
        var sampleSize = 1
        while (boundsOptions.outWidth / sampleSize > maxDim || boundsOptions.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap: Bitmap? = BitmapFactory.decodeFile(targetFile.absolutePath, decodeOptions)
        if (bitmap != null) {
            targetFile.absolutePath to bitmap.asImageBitmap()
        } else {
            null to null
        }
    } catch (_: Exception) {
        null to null
    }
}

suspend fun saveBitmapToAppStorage(
    context: Context,
    bitmap: Bitmap,
    prefix: String
): String? = withContext(Dispatchers.IO) {
    try {
        val uploadDir = File(context.filesDir, "uploads").apply { mkdirs() }
        val targetFile = File(uploadDir, "${prefix}_${System.currentTimeMillis()}.png")
        FileOutputStream(targetFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        targetFile.absolutePath
    } catch (_: Exception) {
        null
    }
}
