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
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            )
            return
        }

        val downloadTask = ImageDownloadTask(WeakReference(context), imageUrl)
        downloadTask.execute()
    }

    class ImageDownloadTask(
        private val contextReference: WeakReference<Context>,
        private val imageUrl: String
    ) : AsyncTask<Void, Void, Bitmap>() {

        override fun doInBackground(vararg params: Void): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                val context = contextReference.get()
                if (context != null) {
                    bitmap = Glide.with(context).asBitmap().load(imageUrl).submit().get()
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error downloading image: ${e.message}")
            }
            return bitmap
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            val context = contextReference.get()
            if (context != null && bitmap != null) {
                val savedImagePath = saveImageToGallery(context, bitmap)
                if (savedImagePath != null && savedImagePath.isNotEmpty()) {
                    Log.e(LOG_TAG, "Image saved successfully to: $savedImagePath")
                } else {
                    Log.e(LOG_TAG, "Failed to save image to gallery")
                }
            }
        }

        private fun saveImageToGallery(context: Context, bitmap: Bitmap): String? {
            val imageFileName = "${context.getString(R.string.app_name)}_${System.currentTimeMillis()}.jpg"
            val storageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                context.getString(R.string.app_name)
            )

            var success = true
            if (!storageDir.exists()) {
                success = storageDir.mkdirs()
            }
            if (success) {
                val imageFile = File(storageDir, imageFileName)
                val savedImagePath = imageFile.absolutePath
                try {
                    val fOut = FileOutputStream(imageFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                    fOut.close()
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Error saving image: ${e.message}")
                    return null
                }

                // Add the image to the system gallery
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.TITLE, imageFileName)
                    put(
                        MediaStore.Images.Media.DESCRIPTION,
                        "Image saved by ${context.getString(R.string.app_name)}"
                    )
                    put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.DATA, savedImagePath)
                }

                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                return savedImagePath
            } else {
                Log.e(LOG_TAG, "Failed to create directory")
                return null
            }
        }
    }
}
