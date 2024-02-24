package com.niteshkumarjha.internetphotosearch

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var firestoreDB: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        firestoreDB = FirebaseFirestore.getInstance()

        searchEditText = findViewById(R.id.search_text)
        searchButton = findViewById(R.id.Search_button)

        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString().trim()

            if (searchText.isEmpty()) {
                Toast.makeText(this@MainActivity, "Enter a search keyword", Toast.LENGTH_SHORT).show()
            } else {
                val serialNumber = Build.SERIAL
                val deviceName = Build.DEVICE
                storeSearch(searchText, serialNumber, deviceName)
                openSearchActivity(searchText)
            }
        }
    }

    private fun openSearchActivity(searchText: String) {
        val intent = Intent(this@MainActivity, SearchActivity::class.java)
        intent.putExtra("Search_text", searchText)
        startActivity(intent)
    }

    private fun storeSearch(searchText: String, serialNumber: String, deviceName: String) {
        // Access a Cloud Firestore instance and store the search text along with device details
        val searchDetails = hashMapOf(
            "searchText" to searchText,
            "serialNumber" to serialNumber,
            "deviceName" to deviceName,
            "timestamp" to System.currentTimeMillis()
        )

        // Add a new document with a generated ID
        firestoreDB.collection("searches")
            .add(searchDetails)
            .addOnSuccessListener { documentReference ->
                Log.e("MainActivity", "Search details added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error adding search details", e)
            }
    }
}
