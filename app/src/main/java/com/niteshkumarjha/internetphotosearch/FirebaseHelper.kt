package com.niteshkumarjha.internetphotosearch;

import android.os.Build
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseHelper {
    private const val LOG_TAG = "FirebaseHelper"

    private val firestoreDB by lazy { FirebaseFirestore.getInstance() }

    fun storeSearchDetails(searchText: String) {
        val userId = Build.ID
        val deviceName = Build.DEVICE.toUpperCase()
        val manufacturer = Build.MANUFACTURER
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        Log.e(LOG_TAG, "NITESH_NITESH " +
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

}

