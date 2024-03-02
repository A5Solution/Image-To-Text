package com.example.image_to_text.ui.languages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.image_to_text.R
import com.example.image_to_text.databinding.ActivityLanguagesBinding
import com.example.image_to_text.ui.retrofit.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LanguagesActivity : AppCompatActivity() {
    private lateinit var adapter: CountryAdapter
    private lateinit var binding: ActivityLanguagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerViewCountries.layoutManager = LinearLayoutManager(this)
        fetchData()
    }

    private fun fetchData() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val countries = ApiClient.create().getAllCountries()
                adapter = CountryAdapter(countries)

                // Nullability check for adapterhtt
                if (::adapter.isInitialized) {
                    binding.recyclerViewCountries.adapter = adapter
                } else {
                    Log.e("Error", "Adapter not initialized")
                }
            } catch (e: Exception) {
                // Log the exception message
                Log.e("Error", "Exception: ${e.message}")
            }
        }
    }

}
