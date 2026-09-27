package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.media.ExifInterface
import java.io.InputStream

object AttachmentHelper {

    fun uriToScaledBitmap(context: Context, uri: Uri, maxDimension: Int = 1024): Bitmap? {
        return try {
            val contentResolver = context.contentResolver

            // Determine dimensions
            var inputStream: InputStream? = contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // Decode actual bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (bitmap == null) return null

            // Correct orientation if needed
            fixOrientation(context, uri, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun fixOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    else -> return bitmap
                }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } ?: bitmap
        } catch (e: Exception) {
            bitmap
        }
    }

    fun readTextFromUri(context: Context, uri: Uri, maxChars: Int = 15000): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                val content = reader.readText()
                if (content.length > maxChars) {
                    content.take(maxChars) + "\n\n...[Document truncated for prompt]"
                } else {
                    content
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = "attachment"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex) ?: "attachment"
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        return name
    }
}
