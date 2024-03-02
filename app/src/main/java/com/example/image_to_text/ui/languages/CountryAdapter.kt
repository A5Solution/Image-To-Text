package com.example.image_to_text.ui.languages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.image_to_text.databinding.ItemCountryBinding // View Binding ka import
import com.example.image_to_text.ui.retrofit.Country

class CountryAdapter(private val countries: List<Country>) : RecyclerView.Adapter<CountryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false) // View Binding ka upyog
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val country = countries[position]
        holder.bind(country)
    }

    override fun getItemCount(): Int {
        return countries.size
    }

    class ViewHolder(private val binding: ItemCountryBinding) : RecyclerView.ViewHolder(binding.root) { // ViewHolder class ke constructor mein binding object liya gaya hai
        fun bind(country: Country) {
            binding.countryName.text = country.name
            Glide.with(binding.root.context)
                .load(country.flag)
                .into(binding.countryFlag)
            val languages = country.languages.joinToString { it.name }
            binding.countryLanguages.text = languages
        }
    }
}
