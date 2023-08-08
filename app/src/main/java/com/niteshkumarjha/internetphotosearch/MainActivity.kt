package com.niteshkumarjha.internetphotosearch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchEditText = findViewById(R.id.search_text)
        searchButton = findViewById(R.id.Search_button)

        searchButton.setOnClickListener(View.OnClickListener {
            val searchText = searchEditText.text.toString().trim()

            if (searchText.isEmpty()) {
                Toast.makeText(this@MainActivity, "Enter a search keyword", Toast.LENGTH_SHORT)
                    .show()
            } else {
                openSearchActivity(searchText);
            }
        })
    }

    private fun openSearchActivity(searchText: String) {
        val intent = Intent(this@MainActivity, SearchActivity::class.java)
        intent.putExtra("Search_text", searchText)
        startActivity(intent)
    }
}
