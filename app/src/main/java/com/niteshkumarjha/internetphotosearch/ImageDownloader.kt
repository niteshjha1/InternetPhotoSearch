package com.niteshkumarjha.internetphotosearch

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

object ImageDownloader {
    private const val LOG_TAG = "ImageDownloader"
    private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 123

    fun saveImageToGallery(context: Context, imageUrl: String) {
        Log.d(LOG_TAG, "NITESH_NITESH saveImageToGallery called ")
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val permissionStatus = permissions.map {
            ContextCompat.checkSelfPermission(context, it)
        }

        if (permissionStatus.any { it != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(
                context as Activity,
                permissions,
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            )
            return
        }

        val downloadTask = ImageDownloadTask(WeakReference(context), imageUrl)
        downloadTask.execute()
    }


    class ImageDownloadTask(
        private val contextReference: WeakReference<Context>, private val imageUrl: String
    ) : AsyncTask<Void, Void, Bitmap>() {

        override fun doInBackground(vararg params: Void): Bitmap? {
            Log.d(LOG_TAG, "NITESH_NITESH doInBackground called ")
            var bitmap: Bitmap? = null
            try {
                val context = contextReference.get()
                if (context != null) {
                    bitmap = Glide.with(context).asBitmap().load(imageUrl).submit().get()
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "NITESH_NITESH Error downloading image: ${e.message}")
            }
            return bitmap
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            Log.d(LOG_TAG, "NITESH_NITESH onPostExecute called ")
            val context = contextReference.get()
            if (context != null && bitmap != null) {
                val savedImagePath = saveImageToGallery(context, bitmap)
                if (savedImagePath != null && savedImagePath.isNotEmpty()) {
                    Log.e(LOG_TAG, "NITESH_NITESH Image saved successfully to: $savedImagePath")
                } else {
                    Log.e(LOG_TAG, "NITESH_NITESH Failed to save image to gallery")
                }
            }
        }

        private fun saveImageToGallery(context: Context, bitmap: Bitmap): String? {
            Log.d(LOG_TAG, "NITESH_NITESH saveImageToGallery called 3 ")
            val directoryName = context.getString(R.string.app_name)
                .replace(" ", "_") // Replace spaces with underscores
            val imageFileName = "${directoryName}_${System.currentTimeMillis()}.jpg"

            val storageDir = File(context.filesDir, directoryName)

            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e(LOG_TAG, "NITESH_NITESH Failed to create directory")
                    return null
                }
            }

            val imageFile = File(storageDir, imageFileName)
            val savedImagePath = imageFile.absolutePath
            return try {
                val fOut = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
                Log.d(LOG_TAG, "NITESH_NITESH Image saved successfully to: $savedImagePath")
                savedImagePath
            } catch (e: Exception) {
                Log.e(LOG_TAG, "NITESH_NITESH Error saving image: ${e.message}")
                null
            }
        }
    }
    }
