package com.niteshkumarjha.internetphotosearch

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.HashMap


class SearchActivity : AppCompatActivity() {

    private val API_KEY = "37ad288835e4c64fc0cb8af3f3a1a65d"
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

        val searchText = intent.getStringExtra("Search_text")

        if (searchText != null) {
            if (!searchText.isEmpty()) {
                performImageSearch(searchText)
                searchText == "";
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
        // Set up parameters for the API request
        val parameters: MutableMap<String, String> = HashMap()
        parameters.put("method", METHOD_SEARCH)
        parameters.put("api_key", API_KEY)
        parameters.put("format", "json")
        parameters.put("nojsoncallback", "1")
        parameters.put("safe_search", "1")
        parameters.put("text", searchText)

        val retrofit = Retrofit.Builder().baseUrl("https://api.flickr.com/services/rest/")
            .addConverterFactory(GsonConverterFactory.create()).build()

        val flickrApi = retrofit.create<FlickrApi>(FlickrApi::class.java)

        val call = flickrApi.getPhotos(parameters)

        // Enqueue the call for asynchronous execution
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (!response.isSuccessful) {
                    Log.e("API Response", "Code: " + response.code())
                    return
                }
                val result: MutableList<PhotoModel> = ArrayList()
                val photos = response.body()!!.getAsJsonObject("photos")
                if (photos != null) {
                    val photoArr = photos.getAsJsonArray("photo")
                    for (i in 0 until photoArr.size()) {
                        val itemObj = photoArr[i].asJsonObject

                        // PhotoModel object and add to result list
                        val item = PhotoModel(
                            itemObj.getAsJsonPrimitive("id").asString,
                            itemObj.getAsJsonPrimitive("secret").asString,
                            itemObj.getAsJsonPrimitive("server").asString,
                            itemObj.getAsJsonPrimitive("farm").asString
                        )
                        result.add(item)
                    }
                    mAdapter.addAll(result)
                    mAdapter.notifyDataSetChanged()
                } else {
                    Log.e("API Response", "No 'photos' object found in the response")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                // Handle API call failure
                Log.e("API Failure", t.toString())
            }
        })
    }

    private fun clearResults() {
        mAdapter.list.clear()
        mAdapter.notifyDataSetChanged()
    }

    private inner class ImageDownloadTask(
        private val contextReference: WeakReference<Context>, private val imageUrl: String
    ) : AsyncTask<Void, Void, Bitmap>() {

        override fun doInBackground(vararg params: Void): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                val context = contextReference.get()
                if (context != null) {
                    bitmap = Glide.with(context).asBitmap().load(imageUrl).submit().get()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            val context = contextReference.get()
            if (context != null && bitmap != null) {
                // save the image to the gallery
            }
        }
    }

    private fun showImageDialog(photo: PhotoModel) {
        val builder = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_view, null)
        val context = this

        val dialogImageView = dialogView.findViewById<ImageView>(R.id.dialog_image)
        val saveButton = dialogView.findViewById<Button>(R.id.dialog_save_button)

        Glide.with(this).load(photo.url).into(dialogImageView)

        saveButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val downloadTask = ImageDownloadTask(WeakReference(context), photo.url)
                downloadTask.execute()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE
                )
            }
        }

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
    }

    companion object {
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 123
    }
}