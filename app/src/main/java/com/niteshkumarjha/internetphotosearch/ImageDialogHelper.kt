package com.niteshkumarjha.internetphotosearch

import android.app.Activity
import android.app.AlertDialog
import android.Manifest;
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.niteshkumarjha.internetphotosearch.ImageDownloader.ImageDownloadTask
import java.lang.ref.WeakReference

class ImageDialogHelper(private val activity: Activity) {
    public val LOG_TAG = "ImageDialogHelper"

    private var imageUrl: String? = null

    fun showImageDialog(photo: PhotoModel) {
        Log.d(LOG_TAG, "NITESH_NITESH showImageDialog ")
        val builder = AlertDialog.Builder(activity)
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_image_view, null)

        val dialogImageView = dialogView.findViewById<ImageView>(R.id.dialog_image)
        val saveButton = dialogView.findViewById<Button>(R.id.dialog_save_button)

        Glide.with(activity).load(photo.url).into(dialogImageView)

        saveButton.setOnClickListener {
            imageUrl = photo.url
            if (ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                saveImageToGallery()
            } else {
                requestStoragePermission()
            }
        }

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
    }

    fun saveImageToGallery() {
        Log.d(LOG_TAG, "NITESH_NITESH saveImageToGallery clicked")
        imageUrl?.let {
            ImageDownloader.saveImageToGallery(activity, it)
        }
    }

    private fun requestStoragePermission() {
        Log.d(LOG_TAG, "NITESH_NITESH requestStoragePermission ")
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE_WRITE_EXTERNAL_STORAGE
        )
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        Log.d(LOG_TAG, "NITESH_NITESH onRequestPermissionsResult ")
        when (requestCode) {
            REQUEST_CODE_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImageToGallery()
                } else {
                    Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 123
    }
}