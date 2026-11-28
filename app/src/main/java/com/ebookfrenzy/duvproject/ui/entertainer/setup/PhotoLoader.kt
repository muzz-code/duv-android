package com.ebookfrenzy.duvproject.ui.entertainer.setup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build

/**
 * Decodes a picked photo at roughly [sizePx] on its shorter side instead of full resolution, so a 12 MP camera
 * photo doesn't need ~50 MB of memory just to fill a 130dp circle. Returns null if the image can't be read.
 */
fun loadPhoto(context: Context, uri: Uri, sizePx: Int): Bitmap? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // ImageDecoder also applies the photo's EXIF rotation.
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            val shorter = minOf(info.size.width, info.size.height)
            if (shorter > sizePx) {
                val scale = sizePx.toFloat() / shorter
                decoder.setTargetSize((info.size.width * scale).toInt(), (info.size.height * scale).toInt())
            }
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (minOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= sizePx) sample *= 2
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        }
    }
}.getOrNull()

/** Scales the picked photo to at most [maxPx] on its longer side and compresses it to JPEG for upload. */
fun compressForUpload(context: Context, uri: Uri, maxPx: Int = 1080, quality: Int = 85): ByteArray? {
    val bitmap = loadPhoto(context, uri, maxPx) ?: return null
    val longer = maxOf(bitmap.width, bitmap.height)
    val scaled = if (longer > maxPx) {
        val scale = maxPx.toFloat() / longer
        Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
    } else bitmap
    return java.io.ByteArrayOutputStream().use { out ->
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        out.toByteArray()
    }
}

/** Decodes downloaded photo bytes at roughly [sizePx] on the shorter side. */
fun decodePhoto(bytes: ByteArray, sizePx: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    var sample = 1
    while (minOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= sizePx) sample *= 2
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
}
