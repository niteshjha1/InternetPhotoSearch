package com.niteshkumarjha.internetphotosearch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.niteshkumarjha.internetphotosearch.FirebaseHelper.fetchApiKeyFromFirebase
import com.niteshkumarjha.internetphotosearch.FirebaseHelper.storeSearchDetails


class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var firestoreDB: FirebaseFirestore
    private val LOG_TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        firestoreDB = FirebaseFirestore.getInstance()

        fetchApiKeyFromFirebase { }

        searchEditText = findViewById(R.id.search_text)
        searchButton = findViewById(R.id.Search_button)

        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString().trim()

            if (searchText.isEmpty()) {
                Toast.makeText(this@MainActivity, "Enter a search keyword", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Log.e(LOG_TAG, "Search Keyword : $searchText")
                storeSearchDetails(searchText)
                openSearchActivity(searchText)
            }
        }
    }

    private fun openSearchActivity(searchText: String) {
        val intent = Intent(this@MainActivity, SearchActivity::class.java)
        intent.putExtra("Search_text", searchText)
        startActivity(intent)
    }

}
