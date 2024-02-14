package com.example.image_to_text.ui
import android.view.translation.TranslationResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApertiumService {
    @GET("/json/translate")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String
    ): TranslationResponse
}
