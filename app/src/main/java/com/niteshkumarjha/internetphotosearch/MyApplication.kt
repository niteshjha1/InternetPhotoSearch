package com.niteshkumarjha.internetphotosearch;

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class MyApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: MyApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

