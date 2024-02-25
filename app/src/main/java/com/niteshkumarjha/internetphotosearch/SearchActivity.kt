package com.niteshkumarjha.internetphotosearch


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.niteshkumarjha.internetphotosearch.FirebaseHelper.getFlickerApiKeyFromFirebase
import com.niteshkumarjha.internetphotosearch.FirebaseHelper.storeSearchDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList
import java.util.HashMap


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

        //toolbar
        val customToolbar: Toolbar = findViewById(R.id.custom_toolbar)
        setSupportActionBar(customToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val searchText = intent.getStringExtra("Search_text")

        if (!searchText.isNullOrEmpty()) {
            performImageSearch(searchText)
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
            val imageDialogHelper = ImageDialogHelper(this)
            imageDialogHelper.showImageDialog(photo)
        }
    }

    private fun performImageSearch(searchText: String) {
        api_key = getFlickerApiKeyFromFirebase()
        val parameters: MutableMap<String, String> = HashMap()
        parameters.put("method", METHOD_SEARCH)
        parameters.put("api_key", api_key)
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
                Log.e("API Failure", t.toString())
            }
        })
    }

    private fun clearResults() {
        mAdapter.list.clear()
        mAdapter.notifyDataSetChanged()
    }

}