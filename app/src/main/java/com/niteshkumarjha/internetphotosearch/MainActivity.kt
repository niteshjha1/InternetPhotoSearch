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
import java.text.SimpleDateFormat
import java.util.*

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

        searchEditText = findViewById(R.id.search_text)
        searchButton = findViewById(R.id.Search_button)

        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString().trim()

            if (searchText.isEmpty()) {
                Toast.makeText(this@MainActivity, "Enter a search keyword", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val userId = Build.ID
                val userName = Build.USER;
                val deviceName = Build.DEVICE.toUpperCase(Locale.getDefault())
                val manufacturer = Build.MANUFACTURER
                val timeStamp =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                Log.e(
                    LOG_TAG, "NITESH_NITESH Serial Number : $userId || " +
                            "Device Name : $deviceName || " +
                            "Manufacturer : $manufacturer || " +
                            "userName : $userName || " +
                            "Manufacturer : $manufacturer || " +
                            "Timestamp : $timeStamp"
                )
                storeSearch(userId, deviceName, searchText, manufacturer, timeStamp)
                openSearchActivity(searchText)
            }
        }
    }

    private fun openSearchActivity(searchText: String) {
        val intent = Intent(this@MainActivity, SearchActivity::class.java)
        intent.putExtra("Search_text", searchText)
        startActivity(intent)
    }

    private fun storeSearch(
        userId: String,
        deviceName: String,
        manufacturer: String,
        timeStamp: String,
        searchText: String,
    ) {
        val searchDetails = hashMapOf(
            "userId" to userId,
            "deviceName" to deviceName,
            "manufacturer" to manufacturer,
            "searchText" to searchText,
            "timestamp" to timeStamp
        )

        firestoreDB.collection("searches")
            .add(searchDetails)
            .addOnSuccessListener { documentReference ->
                val message = "Search details added with ID: ${documentReference.id}"
                Log.e(LOG_TAG, message)
            }
            .addOnFailureListener { e ->
                val errorMessage = "Error adding search details: ${e.message}"
                Log.e(LOG_TAG, errorMessage, e)
            }
    }
}
