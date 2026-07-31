package com.skul9x.geotagging.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

sealed interface ExifWriteResult {
    object Success : ExifWriteResult
    data class CopyWriteSuccess(val newUri: Uri) : ExifWriteResult
    data class PermissionDenied(val exception: SecurityException) : ExifWriteResult
    data class FileNotFound(val exception: Exception) : ExifWriteResult
    data class WriteFailed(val exception: Exception) : ExifWriteResult
}

object ExifUtils {
    private const val TAG = "ExifUtils"

    private fun safeLogE(tag: String, msg: String, tr: Throwable? = null) {
        try {
            if (tr != null) Log.e(tag, msg, tr) else Log.e(tag, msg)
        } catch (t: Throwable) {
            // Ignored in non-mocked log unit tests
        }
    }

    private fun safeLogD(tag: String, msg: String) {
        try {
            Log.d(tag, msg)
        } catch (t: Throwable) {
            // Ignored in non-mocked log unit tests
        }
    }

    /**
     * Checks if the URI is a Photo Picker URI (read-only, cannot be written to directly).
     */
    fun isPickerUri(uri: Uri): Boolean {
        val authority = uri.authority ?: ""
        val path = uri.path ?: ""
        return authority.contains("photopicker", ignoreCase = true)
                || path.startsWith("/picker/")
    }

    fun readLocation(context: Context, uri: Uri): Pair<Double, Double>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val latLong = FloatArray(2)
                if (exif.getLatLong(latLong)) {
                    Pair(latLong[0].toDouble(), latLong[1].toDouble())
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            safeLogE(TAG, "Error reading EXIF location for uri: $uri", e)
            null
        }
    }

    /**
     * Direct write to a writable URI (SAF tree URIs, MediaStore URIs the app owns).
     */
    fun writeLocation(context: Context, uri: Uri, latitude: Double, longitude: Double): ExifWriteResult {
        return try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "rw")
                ?: return ExifWriteResult.FileNotFound(
                    FileNotFoundException("File descriptor is null for URI: $uri")
                ).also {
                    safeLogE(TAG, "File descriptor is null for uri: $uri")
                }

            try {
                val exif = ExifInterface(pfd.fileDescriptor)
                exif.setLatLong(latitude, longitude)
                exif.saveAttributes()
                ExifWriteResult.Success
            } finally {
                try {
                    pfd.close()
                } catch (t: Throwable) {
                    // Safe close ignore
                }
            }
        } catch (e: SecurityException) {
            safeLogE(TAG, "Permission denied writing EXIF location for uri: $uri", e)
            ExifWriteResult.PermissionDenied(e)
        } catch (e: FileNotFoundException) {
            safeLogE(TAG, "File not found writing EXIF location for uri: $uri", e)
            ExifWriteResult.FileNotFound(e)
        } catch (e: IOException) {
            safeLogE(TAG, "IO error writing EXIF location for uri: $uri", e)
            ExifWriteResult.WriteFailed(e)
        } catch (e: Exception) {
            safeLogE(TAG, "Failed writing EXIF location for uri: $uri", e)
            ExifWriteResult.WriteFailed(e)
        }
    }

    /**
     * Copy-Modify-Save approach for read-only URIs (Photo Picker).
     *
     * 1. Copies the image from the picker URI to a temp file in cache
     * 2. Writes EXIF GPS data to the temp file
     * 3. Saves the modified image as a new entry in MediaStore
     * 4. Returns CopyWriteSuccess with the new writable MediaStore URI
     */
    fun writeLocationViaCopy(
        context: Context,
        sourceUri: Uri,
        fileName: String,
        latitude: Double,
        longitude: Double
    ): ExifWriteResult {
        var tempFile: File? = null
        try {
            // Step 1: Copy source image to temp file
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return ExifWriteResult.FileNotFound(
                    FileNotFoundException("Cannot open input stream for URI: $sourceUri")
                ).also {
                    safeLogE(TAG, "Cannot open input stream for picker uri: $sourceUri")
                }

            tempFile = File(context.cacheDir, "exif_temp_${System.currentTimeMillis()}_$fileName")
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            safeLogD(TAG, "Copied picker image to temp: ${tempFile.absolutePath} (${tempFile.length()} bytes)")

            // Step 2: Write EXIF GPS to the temp file
            val exif = ExifInterface(tempFile.absolutePath)
            exif.setLatLong(latitude, longitude)
            exif.saveAttributes()

            safeLogD(TAG, "EXIF GPS written to temp file: $latitude, $longitude")

            // Step 3: Save modified image to MediaStore as a new entry
            val newUri = saveToMediaStore(context, tempFile, fileName)
                ?: return ExifWriteResult.WriteFailed(
                    IOException("Failed to insert image into MediaStore")
                ).also {
                    safeLogE(TAG, "Failed to insert modified image into MediaStore")
                }

            safeLogD(TAG, "Saved geotagged copy to MediaStore: $newUri")

            return ExifWriteResult.CopyWriteSuccess(newUri)

        } catch (e: SecurityException) {
            safeLogE(TAG, "Permission denied in copy-modify-save for uri: $sourceUri", e)
            return ExifWriteResult.PermissionDenied(e)
        } catch (e: FileNotFoundException) {
            safeLogE(TAG, "File not found in copy-modify-save for uri: $sourceUri", e)
            return ExifWriteResult.FileNotFound(e)
        } catch (e: IOException) {
            safeLogE(TAG, "IO error in copy-modify-save for uri: $sourceUri", e)
            return ExifWriteResult.WriteFailed(e)
        } catch (e: Exception) {
            safeLogE(TAG, "Failed copy-modify-save for uri: $sourceUri", e)
            return ExifWriteResult.WriteFailed(e)
        } finally {
            // Cleanup temp file
            try {
                tempFile?.delete()
            } catch (t: Throwable) {
                // Ignore cleanup errors
            }
        }
    }

    /**
     * Saves a file to MediaStore Images collection and returns the new content URI.
     */
    private fun saveToMediaStore(context: Context, file: File, displayName: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "geotagged_$displayName")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Geotagging")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return null

        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: run {
                // Failed to open output stream, clean up the inserted row
                context.contentResolver.delete(uri, null, null)
                return null
            }

            // Mark as no longer pending (Android Q+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val updateValues = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                context.contentResolver.update(uri, updateValues, null, null)
            }

            return uri
        } catch (e: Exception) {
            // Clean up on failure
            try {
                context.contentResolver.delete(uri, null, null)
            } catch (t: Throwable) {
                // Ignore
            }
            throw e
        }
    }
}