package com.example.image_to_text.ui.retrofit

// ApiService.kt
import retrofit2.http.GET

interface ApiService {
    @GET("all")
    suspend fun getAllCountries(): List<Country>
}
data class Country(
    val name: String,
    val flag: String,
    val languages: List<Language>
)
data class Language(
    val name: String,
    val nativeName: String,
    val iso639_1: String
)
