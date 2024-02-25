package com.niteshkumarjha.internetphotosearch;

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseHelper {
    private const val LOG_TAG = "FirebaseHelper"
    private lateinit var flickerApiKey: String

    private val firestoreDB by lazy { FirebaseFirestore.getInstance() }

    fun fetchApiKeyFromFirebase(callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef: DocumentReference =
            db.collection("/FlickerApiKey").document("zmPqLYzAvs71OjsAqZKs")

        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documentSnapshot = task.result
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    flickerApiKey = documentSnapshot.getString("apikey").toString()
                    if (flickerApiKey.isEmpty()) {
                        Log.e(LOG_TAG, "API Key is empty")
                    } else {
                        Log.d(LOG_TAG, "API Key fetched successfully")
                    }
                } else {
                    Log.e(LOG_TAG, "Document does not exist")
                }
            } else {
                Log.e(LOG_TAG, "Error getting documents: ", task.exception)
            }
            callback.invoke(flickerApiKey)
        }
    }

    fun storeSearchDetails(searchText: String) {
        val userId = Build.ID
        val deviceName = Build.DEVICE.uppercase(Locale.ROOT)
        val manufacturer = Build.MANUFACTURER
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        Log.e(
            LOG_TAG, "NITESH_NITESH " +
                    "Device Name : $deviceName || " +
                    "Manufacturer : $manufacturer || " +
                    "searchText : $searchText || " +
                    "Timestamp : $timeStamp  || " +
                    "User ID : $userId || "
        )

        val searchDetails = hashMapOf(
            "deviceName" to deviceName,
            "manufacturer" to manufacturer,
            "searchText" to searchText,
            "timestamp" to timeStamp,
            "userId" to userId,
        )

        firestoreDB.collection("searches").add(searchDetails)
            .addOnSuccessListener { documentReference ->
                val message = "Search details added with ID: ${documentReference.id}"
                Log.e(LOG_TAG, message)
            }.addOnFailureListener { e ->
                val errorMessage = "Error adding search details: ${e.message}"
                Log.e(LOG_TAG, errorMessage, e)
            }
    }

    fun getFlickerApiKeyFromFirebase(): String {
        return if (::flickerApiKey.isInitialized) {
            flickerApiKey
        } else {
            Log.e(LOG_TAG, "Flicker API key has not been initialized yet")
            ""
        }
    }
}
