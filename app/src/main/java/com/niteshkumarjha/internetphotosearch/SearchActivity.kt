package com.niteshkumarjha.internetphotosearch

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.niteshkumarjha.internetphotosearch.FirebaseHelper.getFlickerApiKeyFromFirebase
import com.niteshkumarjha.internetphotosearch.FirebaseHelper.storeSearchDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.*

class SearchActivity : AppCompatActivity() {
    private var api_key = ""
    private val METHOD_SEARCH = "flickr.photos.search"

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var clearButton: Button
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: PhotoAdapter

    private val COLUMN_NUM = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Toolbar setup
        val customToolbar: Toolbar = findViewById(R.id.custom_toolbar)
        setSupportActionBar(customToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val searchText = intent.getStringExtra("Search_text")
        searchText?.let {
            if (it.isNotEmpty()) {
                performImageSearch(it)
            }
        }

        searchEditText = findViewById(R.id.search_text)
        searchButton = findViewById(R.id.Search_button)
        clearButton = findViewById(R.id.Clear_button)
        mRecyclerView = findViewById(R.id.recycler_view)
        mRecyclerView.setHasFixedSize(true)

        mRecyclerView.layoutManager = GridLayoutManager(this, COLUMN_NUM)
        mAdapter = PhotoAdapter(this, ArrayList())
        mRecyclerView.adapter = mAdapter

        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString().trim()
            if (searchText.isEmpty()) {
                Toast.makeText(this, "Enter a search keyword", Toast.LENGTH_SHORT).show()
            } else {
                storeSearchDetails(searchText)
                performImageSearch(searchText)
            }
        }

        clearButton.setOnClickListener {
            clearResults()
        }

        mAdapter.setOnItemClickListener { photo ->
            showImageDialog(photo)
        }
    }

    private fun performImageSearch(searchText: String) {
        api_key = getFlickerApiKeyFromFirebase()
        val parameters: MutableMap<String, String> = HashMap()
        parameters["method"] = METHOD_SEARCH
        parameters["api_key"] = api_key
        parameters["format"] = "json"
        parameters["nojsoncallback"] = "1"
        parameters["safe_search"] = "1"
        parameters["text"] = searchText

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/services/rest/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val flickrApi = retrofit.create(FlickrApi::class.java)
        val call = flickrApi.getPhotos(parameters)

        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (!response.isSuccessful) {
                    Log.e("API Response", "Code: ${response.code()}")
                    return
                }
                val result: MutableList<PhotoModel> = ArrayList()
                val photos = response.body()?.getAsJsonObject("photos")
                photos?.getAsJsonArray("photo")?.forEach { photo ->
                    photo.asJsonObject.let {
                        result.add(
                            PhotoModel(
                                it["id"].asString,
                                it["secret"].asString,
                                it["server"].asString,
                                it["farm"].asString
                            )
                        )
                    }
                }
                mAdapter.addAll(result)
                mAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("API Failure", t.toString())
            }
        })
    }

    private fun clearResults() {
        mAdapter.list.clear()
        mAdapter.notifyDataSetChanged()
    }

    private inner class ImageDownloadTask(
        private val contextReference: WeakReference<Context>,
        private val imageUrl: String
    ) : AsyncTask<Void, Void, Bitmap>() {

        override fun doInBackground(vararg params: Void): Bitmap? {
            return try {
                Glide.with(contextReference.get()!!)
                    .asBitmap()
                    .load(imageUrl)
                    .submit()
                    .get()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            contextReference.get()?.let { context ->
                bitmap?.let {
                    saveImageToGallery(context, it)
                } ?: Toast.makeText(context, "Failed to download image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToGallery(context: Context, bitmap: Bitmap) {
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                Toast.makeText(context, "Image saved to gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImageDialog(photo: PhotoModel) {
        val builder = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_view, null)

        val dialogImageView = dialogView.findViewById<ImageView>(R.id.dialog_image)
        val saveButton = dialogView.findViewById<Button>(R.id.dialog_save_button)

        Glide.with(this).load(photo.url).into(dialogImageView)

        saveButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        REQUEST_CODE_WRITE_EXTERNAL_STORAGE
                    )
                } else {
                    ImageDownloadTask(WeakReference(this), photo.url).execute()
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CODE_WRITE_EXTERNAL_STORAGE
                    )
                } else {
                    ImageDownloadTask(WeakReference(this), photo.url).execute()
                }
            }
        }

        builder.setView(dialogView)
        builder.create().show()
    }

    companion object {
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 123
    }
}
