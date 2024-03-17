package com.niteshkumarjha.internetphotosearch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.niteshkumarjha.internetphotosearch.FirebaseHelper.fetchApiKeyFromFirebase
import com.niteshkumarjha.internetphotosearch.ui.theme.InternetPhotoSearchTheme


class MainActivity : ComponentActivity() {
    private lateinit var firestoreDB: FirebaseFirestore
    private val LOG_TAG = "MainActivity NITESH_NITESH"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(LOG_TAG, "MainActivity onCreate called")

        FirebaseApp.initializeApp(this)
        firestoreDB = FirebaseFirestore.getInstance()

        fetchApiKeyFromFirebase { }

        try {
            setContent {
                InternetPhotoSearchTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        MainActivityUI(
                            openSearchActivity = { searchText ->
                                openSearchActivity(searchText)
                            },
                            openMyGalleryActivity = {
                                Log.d(LOG_TAG, "Open MyGalleryActivity clicked")
                                openMyGalleryActivity()                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Log any exceptions
            Log.e(LOG_TAG, "Error in MainActivity: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun openMyGalleryActivity() {
        Log.d(LOG_TAG, "Open MyGalleryActivity clicked")
        startActivity(Intent(this@MainActivity, MyGalleryActivity::class.java))
    }


    private fun openSearchActivity(searchText: String) {
        Log.d(LOG_TAG, "openSearchActivity")
        val intent = Intent(this@MainActivity, SearchActivity::class.java)
        intent.putExtra("Search_text", searchText)
        startActivity(intent)
    }
}